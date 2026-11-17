import puppeteer from 'puppeteer-core'
const b = await puppeteer.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: true })
const p = await b.newPage()
await p.setViewport({ width: 1100, height: 1500, deviceScaleFactor: 1.5 })
await p.goto('http://localhost:8080/swagger-ui/index.html', { waitUntil: 'networkidle0' })
await p.waitForSelector('.opblock')
await p.screenshot({ path: process.argv[2] })
await b.close()
