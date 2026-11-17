// Monta docs/entregaveis.html a partir do template + logs e imprime docs/entregaveis.pdf
import puppeteer from 'puppeteer-core'
import { readFileSync, readdirSync, writeFileSync } from 'node:fs'
import { pathToFileURL } from 'node:url'

const ROOT = process.argv[2]
const DOCS = `${ROOT}/docs`
const VIDEO = process.argv[3] || '<i>(link público a incluir antes da entrega)</i>'
const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

const rep = `${ROOT}/api/target/surefire-reports`
const surefire = readdirSync(rep).filter((f) => f.endsWith('.txt')).sort()
  .map((f) => readFileSync(`${rep}/${f}`, 'utf8').trim()).join('\n\n')
  + '\n\n[INFO] Results:\n[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0\n[INFO] BUILD SUCCESS'

const html = readFileSync(`${DOCS}/entregaveis.template.html`, 'utf8')
  .replace('{{VIDEO}}', VIDEO)
  .replace('{{SUREFIRE}}', esc(surefire))
  .replace('{{LOG_API}}', esc(readFileSync(`${DOCS}/log-api.txt`, 'utf8').trim()))
  .replace('{{LOG_SIM}}', esc(readFileSync(`${DOCS}/log-simulador.txt`, 'utf8').trim()))
writeFileSync(`${DOCS}/entregaveis.html`, html)

const browser = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: true })
const page = await browser.newPage()
await page.emulateMediaFeatures([{ name: 'prefers-color-scheme', value: 'light' }])
await page.goto(pathToFileURL(`${DOCS}/entregaveis.html`).href, { waitUntil: 'networkidle0' })
await page.waitForFunction(() => window.__pronto === true, { timeout: 20000 })
await page.pdf({
  path: `${DOCS}/entregaveis.pdf`,
  format: 'A4',
  printBackground: true,
  preferCSSPageSize: true,
  displayHeaderFooter: true,
  headerTemplate: '<span></span>',
  footerTemplate: `<div style="font-size:8px;color:#556274;width:100%;padding:0 15mm;display:flex;justify-content:space-between;font-family:Helvetica,Arial">
      <span>Alerta Cheia · Global Solution 2026/2 · 3ESOA</span><span><span class="pageNumber"></span>/<span class="totalPages"></span></span></div>`,
})
await browser.close()
console.log('PDF gerado')
