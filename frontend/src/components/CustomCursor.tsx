import { useEffect, useRef, useState } from "react";

/**
 * Custom animated cursor for desktop pointer devices.
 * Hidden automatically on touch/coarse pointers.
 */
export default function CustomCursor() {
  const dotRef = useRef<HTMLDivElement>(null);
  const ringRef = useRef<HTMLDivElement>(null);
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;

    const mediaQuery = window.matchMedia("(pointer: fine)");
    setEnabled(mediaQuery.matches);

    const handleChange = (event: MediaQueryListEvent) => setEnabled(event.matches);
    mediaQuery.addEventListener("change", handleChange);

    return () => mediaQuery.removeEventListener("change", handleChange);
  }, []);

  useEffect(() => {
    if (!enabled) return;

    const dot = dotRef.current;
    const ring = ringRef.current;
    if (!dot || !ring) return;

    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;
    let ringX = mouseX;
    let ringY = mouseY;
    let rafId = 0;
    let visible = false;

    const handleMove = (event: MouseEvent) => {
      mouseX = event.clientX;
      mouseY = event.clientY;

      if (!visible) {
        visible = true;
        dot.style.opacity = "1";
        ring.style.opacity = "1";
      }

      dot.style.transform = `translate3d(${mouseX}px, ${mouseY}px, 0) translate(-50%, -50%)`;
    };

    const handleLeaveViewport = () => {
      visible = false;
      dot.style.opacity = "0";
      ring.style.opacity = "0";
    };

    const handleMouseDown = () => ring.classList.add("cursor-ring--down");
    const handleMouseUp = () => ring.classList.remove("cursor-ring--down");

    const interactiveSelector =
      'a, button, [role="button"], input, textarea, select, label, summary, [data-cursor="hover"]';

    const handleMouseOver = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null;
      if (target?.closest(interactiveSelector)) {
        ring.classList.add("cursor-ring--hover");
      }
    };

    const handleMouseOut = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null;
      if (target?.closest(interactiveSelector)) {
        ring.classList.remove("cursor-ring--hover");
      }
    };

    const tick = () => {
      ringX += (mouseX - ringX) * 0.18;
      ringY += (mouseY - ringY) * 0.18;
      ring.style.transform = `translate3d(${ringX}px, ${ringY}px, 0) translate(-50%, -50%)`;
      rafId = requestAnimationFrame(tick);
    };

    window.addEventListener("mousemove", handleMove, { passive: true });
    window.addEventListener("mouseout", (event) => {
      if (!event.relatedTarget) {
        handleLeaveViewport();
      }
    });
    window.addEventListener("mousedown", handleMouseDown);
    window.addEventListener("mouseup", handleMouseUp);
    document.addEventListener("mouseover", handleMouseOver);
    document.addEventListener("mouseout", handleMouseOut);

    rafId = requestAnimationFrame(tick);
    document.documentElement.classList.add("custom-cursor-active");

    return () => {
      cancelAnimationFrame(rafId);
      window.removeEventListener("mousemove", handleMove);
      window.removeEventListener("mousedown", handleMouseDown);
      window.removeEventListener("mouseup", handleMouseUp);
      document.removeEventListener("mouseover", handleMouseOver);
      document.removeEventListener("mouseout", handleMouseOut);
      document.documentElement.classList.remove("custom-cursor-active");
    };
  }, [enabled]);

  if (!enabled) return null;

  return (
    <>
      <div
        ref={ringRef}
        className="cursor-ring pointer-events-none fixed left-0 top-0 z-[9999] h-9 w-9 rounded-full border opacity-0"
        style={{
          borderColor: "color-mix(in oklab, var(--neon-blue) 70%, transparent)",
          boxShadow:
            "0 0 18px color-mix(in oklab, var(--neon-purple) 60%, transparent), inset 0 0 10px color-mix(in oklab, var(--neon-blue) 30%, transparent)",
          transition:
            "width 220ms ease, height 220ms ease, background-color 220ms ease, border-color 220ms ease, opacity 220ms ease",
          mixBlendMode: "screen",
        }}
      />
      <div
        ref={dotRef}
        className="pointer-events-none fixed left-0 top-0 z-[10000] h-2 w-2 rounded-full opacity-0"
        style={{
          backgroundColor: "var(--neon-cyan)",
          boxShadow: "0 0 10px var(--neon-cyan)",
          transition: "opacity 200ms ease",
        }}
      />
    </>
  );
}
