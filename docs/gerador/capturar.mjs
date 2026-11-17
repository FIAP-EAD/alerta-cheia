// Captura evidencias (telas do app, Swagger, logs) para o PDF de entregaveis.
import puppeteer from 'puppeteer-core'
import { execFileSync } from 'node:child_process'
import { mkdirSync, writeFileSync } from 'node:fs'

const OUT = process.argv[2]
const SIM = process.argv[3]
const IMG = `${OUT}/img`
mkdirSync(IMG, { recursive: true })
const WEB = 'http://localhost:5173'
const API = 'http://localhost:8080'
const espera = (ms) => new Promise((r) => setTimeout(r, ms))

const browser = await puppeteer.launch({
  executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  headless: true,
})
const page = await browser.newPage()
await page.emulateMediaFeatures([{ name: 'prefers-color-scheme', value: 'light' }])
await page.setViewport({ width: 390, height: 844, deviceScaleFactor: 2, isMobile: true, hasTouch: true })

const foto = async (nome) => { await espera(400); await page.screenshot({ path: `${IMG}/${nome}.png` }); console.log('ok', nome) }
async function login(email, senha) {
  await page.goto(`${WEB}/login`, { waitUntil: 'networkidle0' })
  await page.type('#email', email)
  await page.type('#senha', senha)
  await Promise.all([page.waitForFunction(() => location.pathname === '/'), page.click('button.btn-primario')])
  await page.waitForSelector('.status')
}
async function sair() {
  await page.evaluate(() => [...document.querySelectorAll('button')].find((b) => b.textContent === 'Sair').click())
  await page.waitForSelector('#email')
}
const clicarTexto = (txt) => page.evaluate((t) => [...document.querySelectorAll('button,a')].find((b) => b.textContent.trim() === t).click(), txt)

// 1. Login
await page.goto(`${WEB}/login`, { waitUntil: 'networkidle0' })
await foto('01-login')

// 2. Cadastro com validacao
await page.goto(`${WEB}/cadastro`, { waitUntil: 'networkidle0' })
await page.type('#nome', 'Teste')
await page.type('#email', 'email-invalido')
await page.type('#senha', '123')
await page.click('button.btn-primario')
await foto('02-cadastro-validacao')

// 3. Inicio NORMAL
await login('joao@email.com', 'Cidadao@123')
await foto('03-inicio-normal')

// 4. Simulador -> CRITICO
const log = execFileSync('python3', ['-I', SIM, '--cenario', 'enchente', '--intervalo', '0.3', '--ciclos', '8'], { encoding: 'utf8' })
writeFileSync(`${OUT}/log-simulador.txt`, log)
await page.waitForFunction(() => document.querySelector('.status-critico'), { timeout: 20000 })
await foto('04-inicio-critico')

// 5. Detalhe do alerta
await page.click('.cartao-alerta')
await page.waitForSelector('.voltar')
await espera(800)
await foto('05-alerta-detalhe')

// 6. Abrigos
await page.goto(`${WEB}/abrigos`, { waitUntil: 'networkidle0' })
await foto('06-abrigos')

// 7. Novo relato + lista
await page.goto(`${WEB}/relatos/novo`, { waitUntil: 'networkidle0' })
await page.type('#descricao', 'Agua na altura do joelho perto da ponte. Corrego transbordou.')
await page.click('button.btn-primario')
await page.waitForSelector('.toast')
await foto('07-relatos')

// 8. Offline: derruba a rede e espera o proximo polling (10 s) da tela inicial
await page.goto(`${WEB}/`, { waitUntil: 'networkidle0' })
await page.waitForSelector('.status')
await page.setOfflineMode(true)
await page.waitForSelector('.aviso-offline', { timeout: 20000 })
await foto('08-offline')
await page.setOfflineMode(false)

// 9. Admin
await sair()
await login('admin@alertacheia.com', 'Admin@123')
await page.goto(`${WEB}/admin`, { waitUntil: 'networkidle0' })
await foto('09-admin-alertas')
await clicarTexto('Sensores')
await page.waitForSelector('.cartao-titulo')
await foto('10-admin-sensores')
await page.goto(`${WEB}/abrigos`, { waitUntil: 'networkidle0' })
await foto('11-admin-abrigos')

// 10. Swagger (desktop)
await page.setViewport({ width: 1280, height: 1000, deviceScaleFactor: 1.5 })
await page.goto(`${API}/swagger-ui/index.html`, { waitUntil: 'networkidle0' })
await page.waitForSelector('.opblock')
await page.screenshot({ path: `${IMG}/12-swagger.png`, fullPage: true })
console.log('ok swagger')

// 11. Evidencias manuais da API (requisicao + resposta)
const linhas = []
async function chamada(titulo, metodo, path, { body, token, chave } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  if (chave) headers['X-Sensor-Key'] = chave
  const r = await fetch(API + path, { method: metodo, headers, body: body ? JSON.stringify(body) : undefined })
  const txt = await r.text()
  linhas.push(`### ${titulo}\n> ${metodo} ${path}${chave ? '  [X-Sensor-Key]' : ''}${token ? '  [Bearer token]' : ''}${body ? '\n> ' + JSON.stringify(body) : ''}\n< HTTP ${r.status}\n${txt.length > 600 ? txt.slice(0, 600) + ' ...' : txt}\n`)
  return { status: r.status, json: txt ? JSON.parse(txt) : null }
}
const adm = (await chamada('Login ADMIN', 'POST', '/auth/login', { body: { email: 'admin@alertacheia.com', senha: 'Admin@123' } })).json.token
linhas[linhas.length - 1] = linhas[linhas.length - 1].replace(/"token":"[^"]+"/, '"token":"eyJhbGciOi...(omitido)"')
const cid = (await fetch(API + '/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email: 'maria@email.com', senha: 'Cidadao@123' }) }).then((r) => r.json())).token
await chamada('Listar alertas sem token', 'GET', '/alertas')
await chamada('Cidadao tentando criar sensor', 'POST', '/sensores', { token: cid, body: { codigo: 'RIO-009', tipo: 'NIVEL_RIO', bairroId: 1, limiteAlerta: 2, limiteCritico: 3 } })
await chamada('ADMIN cria sensor com limites invalidos', 'POST', '/sensores', { token: adm, body: { codigo: 'RIO-009', tipo: 'NIVEL_RIO', bairroId: 1, limiteAlerta: 3, limiteCritico: 2 } })
await chamada('ADMIN cria sensor valido', 'POST', '/sensores', { token: adm, body: { codigo: 'RIO-009', tipo: 'NIVEL_RIO', bairroId: 4, limiteAlerta: 2, limiteCritico: 3 } })
await chamada('Sensor envia leitura critica', 'POST', '/leituras', { chave: 'sensor-dev-key-123', body: { codigoSensor: 'RIO-009', valor: 3.25 } })
await chamada('Situacao do bairro 4 (cidadao)', 'GET', '/bairros/4/situacao', { token: cid })
await chamada('Abrigo inexistente', 'GET', '/abrigos/999', { token: cid })
writeFileSync(`${OUT}/log-api.txt`, linhas.join('\n'))
console.log('ok api')

await browser.close()
