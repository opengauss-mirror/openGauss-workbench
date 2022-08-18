export const edge1 = {
  ports: {
    groups: {
      top: { position: 'top',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      bottom: { position: 'bottom',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      left: { position: 'left',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      },
      right: { position: 'right',
        attrs: { circle: { r: 5, magnet: true, stroke: '#5F95FF', strokeWidth: 1, fill: '#fff', style: { visibility: 'hidden' }}}
      }
    },
    items: [
      { group: 'top', id: 'port1' },
      { group: 'bottom', id: 'port2' },
      { group: 'left', id: 'port3' },
      { group: 'right', id: 'port4' }
    ]
  }
}
