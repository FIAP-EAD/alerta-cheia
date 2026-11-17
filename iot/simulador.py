"""
Simulador IoT do Alerta Cheia.

Simula 2 sensores do bairro Jardim Romano e envia as leituras para a API (POST /leituras):
  - RIO-002   (NIVEL_RIO):   nivel do corrego em metros. Alerta >= 2.0 m, critico >= 3.0 m
  - CHUVA-003 (PLUVIOMETRO): intensidade da chuva em mm/h. Alerta >= 30, critico >= 50

Usa apenas a biblioteca padrao do Python (nada para instalar).

Exemplos:
  python simulador.py --cenario normal
  python simulador.py --cenario enchente --intervalo 3
  python simulador.py --cenario chuva --ciclos 5 --url http://localhost:8080
"""
import argparse
import json
import random
import time
import urllib.error
import urllib.request

SENSOR_RIO = "RIO-002"
SENSOR_CHUVA = "CHUVA-003"


def gerar_valores(cenario, ciclo):
    """Devolve (nivel_rio_m, chuva_mm_h) para o ciclo atual de cada cenario."""
    if cenario == "normal":
        return round(random.uniform(0.8, 1.4), 2), round(random.uniform(0, 12), 1)
    if cenario == "chuva":  # chuva forte, rio sobe ate a faixa de ATENCAO
        return round(min(1.2 + 0.15 * ciclo, 2.6), 2), round(min(20 + 4 * ciclo, 45), 1)
    # enchente: sobe de forma previsivel ate passar do limite critico (bom para o video)
    return round(1.0 + 0.3 * ciclo, 2), round(min(15 + 6 * ciclo, 75), 1)


def enviar(url, chave, codigo, valor):
    corpo = json.dumps({"codigoSensor": codigo, "valor": valor}).encode("utf-8")
    req = urllib.request.Request(f"{url}/leituras", data=corpo, method="POST",
                                 headers={"Content-Type": "application/json", "X-Sensor-Key": chave})
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        print(f"  [ERRO {e.code}] {codigo}: {e.read().decode('utf-8')}")
    except urllib.error.URLError as e:
        print(f"  [SEM CONEXAO] API indisponivel em {url}: {e.reason}")
    return None


def main():
    p = argparse.ArgumentParser(description="Simulador de sensores do Alerta Cheia")
    p.add_argument("--cenario", choices=["normal", "chuva", "enchente"], default="normal")
    p.add_argument("--url", default="http://localhost:8080")
    p.add_argument("--chave", default="sensor-dev-key-123", help="valor do header X-Sensor-Key")
    p.add_argument("--intervalo", type=float, default=5, help="segundos entre envios")
    p.add_argument("--ciclos", type=int, default=10)
    args = p.parse_args()

    print(f"Simulando cenario '{args.cenario}' -> {args.url} ({args.ciclos} ciclos)\n")
    for ciclo in range(args.ciclos):
        rio, chuva = gerar_valores(args.cenario, ciclo)
        print(f"Ciclo {ciclo + 1}: {SENSOR_RIO}={rio} m | {SENSOR_CHUVA}={chuva} mm/h")
        for codigo, valor in ((SENSOR_RIO, rio), (SENSOR_CHUVA, chuva)):
            resp = enviar(args.url, args.chave, codigo, valor)
            if resp:
                print(f"  {codigo}: situacao {resp['situacao']}")
                if resp.get("alertaGerado"):
                    a = resp["alertaGerado"]
                    print(f"  >>> ALERTA {a['severidade']} criado: {a['mensagem']}")
        time.sleep(args.intervalo)
    print("\nSimulacao finalizada.")


if __name__ == "__main__":
    main()
