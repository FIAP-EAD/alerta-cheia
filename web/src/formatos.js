export function dataHora(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

export function haQuantoTempo(ms) {
  if (!ms) return ''
  const min = Math.round((Date.now() - ms) / 60000)
  if (min < 1) return 'agora'
  if (min < 60) return `ha ${min} min`
  return `ha ${Math.round(min / 60)} h`
}

export const SITUACAO = {
  NORMAL: { rotulo: 'Normal', classe: 'normal', icone: '✓', dica: 'Nenhum risco de enchente no momento.' },
  ATENCAO: { rotulo: 'Atenção', classe: 'atencao', icone: '!', dica: 'Fique atento e evite áreas próximas ao córrego.' },
  CRITICO: { rotulo: 'Crítico', classe: 'critico', icone: '!!', dica: 'Risco alto de alagamento. Procure um abrigo ou local elevado.' },
}
