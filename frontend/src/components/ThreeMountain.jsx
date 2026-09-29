import { useEffect, useRef, useState } from 'react'
import * as THREE from 'three'

function G(x, z, cx, cz, s, a) {
  return a * Math.exp(-((x - cx) * (x - cx) + (z - cz) * (z - cz)) / (2 * s * s))
}

function H(x, z) {
  return (
    G(x, z, -3, 2, 3.2, 3.2) +
    G(x, z, 4, -4, 2.2, 2.1) +
    G(x, z, 5, 5, 2.8, 1.2) -
    G(x, z, -5, -5, 2.6, 1.4) +
    0.25 * Math.sin(x * 0.9) * Math.cos(z * 0.8)
  )
}

function N(h) {
  return Math.min(1, Math.max(0, (h + 1.5) / 5.2))
}

const BEST = { x: -3, z: 2 }

export default function ThreeMountain({ onReadoutUpdate }) {
  const canvasRef = useRef(null)
  const isDarkRef = useRef(false)

  useEffect(() => {
    const cv = canvasRef.current
    if (!cv) return

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const checkDark = () => {
      const t = document.documentElement.getAttribute('data-theme')
      return t ? t === 'dark' : window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    isDarkRef.current = checkDark()

    const R = new THREE.WebGLRenderer({ canvas: cv, antialias: true, alpha: true })
    R.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))

    const S = new THREE.Scene()
    const C = new THREE.PerspectiveCamera(42, 1, 0.1, 100)

    const n = 56
    const g = new THREE.PlaneGeometry(20, 20, n, n)
    g.rotateX(-Math.PI / 2)

    const p = g.attributes.position
    const col = []
    const c0 = new THREE.Color(0x3b5bff)
    const c1 = new THREE.Color(0xff4fa3)
    const c2 = new THREE.Color(0xffe55c)
    const tc = new THREE.Color()

    for (let i = 0; i < p.count; i++) {
      const h = H(p.getX(i), p.getZ(i))
      p.setY(i, h)
      const t = N(h)
      if (t < 0.5) tc.copy(c0).lerp(c1, t * 2)
      else tc.copy(c1).lerp(c2, (t - 0.5) * 2)
      col.push(tc.r, tc.g, tc.b)
    }
    g.setAttribute('color', new THREE.Float32BufferAttribute(col, 3))

    const mesh = new THREE.Mesh(g, new THREE.MeshBasicMaterial({ vertexColors: true }))
    const wm = new THREE.MeshBasicMaterial({
      wireframe: true,
      transparent: true,
      opacity: 0.28,
      color: 0x16081f,
    })
    const wire = new THREE.Mesh(g, wm)
    wire.position.y = 0.012

    const mk = new THREE.Mesh(
      new THREE.SphereGeometry(0.3, 20, 20),
      new THREE.MeshBasicMaterial({ color: 0xffffff })
    )
    const ring = new THREE.Mesh(
      new THREE.RingGeometry(0.5, 0.62, 32),
      new THREE.MeshBasicMaterial({ color: 0x16081f, side: 2 })
    )
    ring.rotation.x = -Math.PI / 2

    S.add(mesh, wire, mk, ring)

    function place(x, z) {
      const y = H(x, z)
      mk.position.set(x, y + 0.05, z)
      ring.position.set(x, y + 0.08, z)
    }
    place(BEST.x, BEST.z)

    function notifyReadout(x, z, live) {
      const sh = 0.1 + 2.4 * N(H(x, z))
      const fast = Math.round(5 + ((x + 10) / 20) * 45)
      const slow = Math.round(20 + ((z + 10) / 20) * 180)
      const verdict = !live
        ? 'Optimal Parameter Peak'
        : sh > 1.8
        ? 'High Statistical Alpha'
        : sh > 1.2
        ? 'Robust Parameter Regime'
        : sh > 0.6
        ? 'Moderate Edge'
        : 'Suboptimal Zone'

      if (onReadoutUpdate) {
        onReadoutUpdate({
          fast,
          slow,
          sharpe: sh.toFixed(2),
          verdict,
        })
      }
    }

    notifyReadout(BEST.x, BEST.z, false)

    let px = 0
    let tpx = 0
    let hov = false
    const ray = new THREE.Raycaster()
    const v = new THREE.Vector2()
    const pl = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0)
    const hit = new THREE.Vector3()

    function size() {
      const w = window.innerWidth
      const h = window.innerHeight
      R.setSize(w, h, false)
      C.aspect = w / h
      C.position.y = w < 700 ? 11 : 9
      C.updateProjectionMatrix()
    }
    window.addEventListener('resize', size)
    size()

    function onPointerMove(e) {
      tpx = e.clientX / window.innerWidth - 0.5
      if (window.scrollY > window.innerHeight * 0.7) return

      v.set((e.clientX / window.innerWidth) * 2 - 1, -(e.clientY / window.innerHeight) * 2 + 1)
      ray.setFromCamera(v, C)

      let y = 0
      let ok = false
      for (let k = 0; k < 3; k++) {
        pl.constant = -y
        if (!ray.ray.intersectPlane(pl, hit)) {
          ok = false
          break
        }
        ok = true
        y = H(hit.x, hit.z)
      }

      if (ok && Math.abs(hit.x) < 10 && Math.abs(hit.z) < 10) {
        hov = true
        place(hit.x, hit.z)
        notifyReadout(hit.x, hit.z, true)
      } else if (hov) {
        hov = false
        place(BEST.x, BEST.z)
        notifyReadout(BEST.x, BEST.z, false)
      }
    }
    window.addEventListener('pointermove', onPointerMove)

    let reqId
    const t0 = performance.now()

    function frame(now) {
      reqId = requestAnimationFrame(frame)
      const op = Math.max(0, 1 - window.scrollY / (window.innerHeight * 0.9))
      cv.style.opacity = op
      if (op <= 0) return

      px += (tpx - px) * 0.05
      const th = 0.45 + (reduce ? 0 : ((now - t0) / 1000) * 0.05) + px * 0.5
      const r = 17
      C.position.x = r * Math.sin(th)
      C.position.z = r * Math.cos(th)
      C.lookAt(window.innerWidth > 900 ? -3.2 : 0, 0.4, 0)

      const dark = checkDark()
      const wireCol = dark ? 0xf6f0ff : 0x16081f
      wm.color.setHex(wireCol)
      ring.material.color.setHex(wireCol)

      R.render(S, C)
    }
    reqId = requestAnimationFrame(frame)

    return () => {
      cancelAnimationFrame(reqId)
      window.removeEventListener('resize', size)
      window.removeEventListener('pointermove', onPointerMove)
      g.dispose()
      mesh.material.dispose()
      wm.dispose()
      mk.geometry.dispose()
      mk.material.dispose()
      ring.geometry.dispose()
      ring.material.dispose()
      R.dispose()
    }
  }, [onReadoutUpdate])

  return <canvas ref={canvasRef} id="c" aria-hidden="true" />
}
