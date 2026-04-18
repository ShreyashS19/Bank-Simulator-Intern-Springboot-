import { useRef, type CSSProperties, type ReactNode } from "react";

interface TiltCardProps {
  children: ReactNode;
  className?: string;
  max?: number;
  scale?: number;
  glare?: boolean;
}

/**
 * Lightweight 3D tilt wrapper with optional glare effect.
 */
export default function TiltCard({
  children,
  className = "",
  max = 12,
  scale = 1.02,
  glare = true,
}: TiltCardProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const innerRef = useRef<HTMLDivElement>(null);
  const glareRef = useRef<HTMLDivElement>(null);
  const rafRef = useRef<number | null>(null);

  const handleMove = (event: React.MouseEvent<HTMLDivElement>) => {
    const container = containerRef.current;
    const inner = innerRef.current;
    if (!container || !inner) return;

    const rect = container.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const percentX = x / rect.width;
    const percentY = y / rect.height;

    const rotateY = (percentX - 0.5) * 2 * max;
    const rotateX = -(percentY - 0.5) * 2 * max;

    if (rafRef.current) cancelAnimationFrame(rafRef.current);

    rafRef.current = requestAnimationFrame(() => {
      inner.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(${scale})`;

      if (glareRef.current) {
        glareRef.current.style.background = `radial-gradient(circle at ${percentX * 100}% ${percentY * 100}%, color-mix(in oklab, var(--neon-cyan) 28%, transparent) 0%, transparent 55%)`;
        glareRef.current.style.opacity = "1";
      }
    });
  };

  const handleLeave = () => {
    const inner = innerRef.current;
    if (!inner) return;

    if (rafRef.current) cancelAnimationFrame(rafRef.current);
    inner.style.transform = "perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1)";

    if (glareRef.current) {
      glareRef.current.style.opacity = "0";
    }
  };

  const innerStyle: CSSProperties = {
    transformStyle: "preserve-3d",
    transition: "transform 400ms cubic-bezier(0.22, 1, 0.36, 1)",
    willChange: "transform",
  };

  return (
    <div
      ref={containerRef}
      onMouseMove={handleMove}
      onMouseLeave={handleLeave}
      className={className}
      style={{ perspective: "1000px" }}
    >
      <div ref={innerRef} style={innerStyle} className="relative h-full w-full">
        {children}
        {glare && (
          <div
            ref={glareRef}
            className="pointer-events-none absolute inset-0 rounded-2xl opacity-0 transition-opacity duration-300"
            style={{ mixBlendMode: "screen" }}
          />
        )}
      </div>
    </div>
  );
}
