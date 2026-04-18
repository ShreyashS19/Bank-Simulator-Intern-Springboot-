import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import CustomCursor from "@/components/CustomCursor";
import TiltCard from "@/components/TiltCard";

if (typeof window !== "undefined") {
  gsap.registerPlugin(ScrollTrigger);
}

type FeatureItem = {
  title: string;
  desc: string;
  img: string;
  icon: JSX.Element;
};

const features: FeatureItem[] = [
  {
    title: "Account Management",
    desc: "Full control over your accounts with real-time balance tracking, spending insights, and automated budgeting tools.",
    img: "https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=800&q=80",
    icon: (
      <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
        <path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    ),
  },
  {
    title: "Instant Transactions",
    desc: "Send and receive money instantly with zero fees. Lightning-fast transfers powered by next-gen infrastructure.",
    img: "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800&q=80",
    icon: (
      <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
        <path d="M13 10V3L4 14h7v7l9-11h-7z" />
      </svg>
    ),
  },
  {
    title: "Secure Payments",
    desc: "Bank-grade encryption protects every transaction. Multi-factor auth and biometric security built in.",
    img: "https://images.unsplash.com/photo-1556742111-a301076d9d18?w=800&q=80",
    icon: (
      <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
        <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
      </svg>
    ),
  },
  {
    title: "Loan Services",
    desc: "Get pre-approved loans in minutes with competitive rates, flexible terms, and a fully digital process.",
    img: "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=800&q=80",
    icon: (
      <svg className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
        <path d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
];

function Navbar() {
  const navRef = useRef<HTMLElement>(null);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const element = navRef.current;
    if (!element) return;

    const handleScroll = () => {
      element.style.backdropFilter = window.scrollY > 40 ? "blur(20px)" : "blur(8px)";
      element.style.borderBottomColor =
        window.scrollY > 40 ? "var(--glass-border)" : "transparent";
    };

    window.addEventListener("scroll", handleScroll, { passive: true });

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);

  return (
    <>
      <nav
        ref={navRef}
        className="fixed left-0 right-0 top-0 z-50 flex items-center justify-between px-6 py-4 transition-all duration-300 md:px-12"
        style={{
          backgroundColor: "oklch(0.09 0.02 270 / 70%)",
          borderBottom: "1px solid transparent",
        }}
      >
        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] text-sm font-black text-white">
            B
          </div>
          <span className="text-xl font-bold tracking-tight text-white">
            Bank <span className="text-[var(--neon-cyan)]">Simulation</span>
          </span>
        </div>

        <div className="hidden items-center gap-8 text-sm text-white/70 md:flex">
          <a href="#features" className="relative py-1 transition-colors hover:text-white">
            Features
          </a>
          <a href="#security" className="relative py-1 transition-colors hover:text-white">
            Security
          </a>
          <a href="#about" className="relative py-1 transition-colors hover:text-white">
            About
          </a>
        </div>

        <div className="hidden items-center gap-3 md:flex">
          <Link
            to="/login"
            className="rounded-lg border border-[var(--glass-border)] px-5 py-2 text-sm text-white transition-all hover:border-[var(--neon-blue)] hover:text-[var(--neon-cyan)]"
          >
            Login
          </Link>
          <Link
            to="/signup"
            className="rounded-lg bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-purple)] px-5 py-2 text-sm font-medium text-white shadow-[0_0_24px_oklch(0.65_0.2_260_/_35%)] transition-all hover:scale-105 hover:shadow-[0_0_36px_oklch(0.65_0.2_260_/_55%)]"
          >
            Register
          </Link>
        </div>

        <button
          className="relative flex h-8 w-8 flex-col items-center justify-center gap-[5px] md:hidden"
          onClick={() => setMobileOpen((value) => !value)}
          aria-label="Toggle menu"
        >
          <span
            className={`block h-[2px] w-5 rounded bg-white transition-all duration-300 ${
              mobileOpen ? "translate-y-[7px] rotate-45" : ""
            }`}
          />
          <span
            className={`block h-[2px] w-5 rounded bg-white transition-all duration-300 ${
              mobileOpen ? "scale-0 opacity-0" : ""
            }`}
          />
          <span
            className={`block h-[2px] w-5 rounded bg-white transition-all duration-300 ${
              mobileOpen ? "-translate-y-[7px] -rotate-45" : ""
            }`}
          />
        </button>
      </nav>

      <div
        className={`fixed inset-0 z-40 transition-all duration-500 md:hidden ${
          mobileOpen ? "visible opacity-100" : "invisible opacity-0"
        }`}
        style={{
          backgroundColor: "oklch(0.09 0.02 270 / 95%)",
          backdropFilter: "blur(24px)",
        }}
      >
        <div
          className={`flex h-full flex-col items-center justify-center gap-8 transition-transform duration-500 ${
            mobileOpen ? "translate-y-0" : "-translate-y-8"
          }`}
        >
          <a
            href="#features"
            onClick={() => setMobileOpen(false)}
            className="text-2xl font-semibold text-white transition-colors hover:text-[var(--neon-cyan)]"
          >
            Features
          </a>
          <a
            href="#security"
            onClick={() => setMobileOpen(false)}
            className="text-2xl font-semibold text-white transition-colors hover:text-[var(--neon-cyan)]"
          >
            Security
          </a>
          <a
            href="#about"
            onClick={() => setMobileOpen(false)}
            className="text-2xl font-semibold text-white transition-colors hover:text-[var(--neon-cyan)]"
          >
            About
          </a>

          <div className="mt-4 flex w-56 flex-col gap-3">
            <Link
              to="/login"
              onClick={() => setMobileOpen(false)}
              className="w-full rounded-lg border border-[var(--glass-border)] py-3 text-center text-sm text-white transition-all hover:border-[var(--neon-blue)]"
            >
              Login
            </Link>
            <Link
              to="/signup"
              onClick={() => setMobileOpen(false)}
              className="w-full rounded-lg bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-purple)] py-3 text-center text-sm font-medium text-white shadow-[0_0_24px_oklch(0.65_0.2_260_/_35%)]"
            >
              Register
            </Link>
          </div>
        </div>
      </div>
    </>
  );
}

function HeroSection() {
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(".hero-title", { y: 60, opacity: 0, duration: 1, ease: "power3.out" });
      gsap.from(".hero-sub", {
        y: 40,
        opacity: 0,
        duration: 1,
        delay: 0.3,
        ease: "power3.out",
      });
      gsap.from(".hero-btns", {
        y: 30,
        opacity: 0,
        duration: 0.8,
        delay: 0.6,
        ease: "power3.out",
      });
      gsap.from(".hero-card", {
        y: 80,
        opacity: 0,
        rotation: 6,
        duration: 1.2,
        delay: 0.5,
        ease: "power3.out",
        stagger: 0.2,
      });

      gsap.to(".hero-card-1", {
        y: -18,
        rotation: -2,
        duration: 3,
        repeat: -1,
        yoyo: true,
        ease: "sine.inOut",
      });

      gsap.to(".hero-card-2", {
        y: 14,
        rotation: 3,
        duration: 3.5,
        repeat: -1,
        yoyo: true,
        ease: "sine.inOut",
        delay: 0.5,
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  return (
    <section ref={sectionRef} className="relative flex min-h-screen items-center overflow-hidden pt-20">
      <div className="absolute inset-0">
        <img
          src="https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=1920&q=80"
          alt="Abstract fintech background"
          className="h-full w-full object-cover opacity-25"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-black/70 via-black/70 to-black/90" />
        <div className="absolute inset-0 bg-gradient-to-r from-[var(--neon-blue)]/10 via-transparent to-[var(--neon-purple)]/10" />
      </div>

      <div className="animate-float-slow absolute left-1/4 top-1/4 h-[500px] w-[500px] rounded-full bg-[var(--neon-blue)] opacity-[0.08] blur-[120px]" />
      <div className="animate-float-reverse absolute bottom-1/4 right-1/4 h-[400px] w-[400px] rounded-full bg-[var(--neon-purple)] opacity-[0.08] blur-[120px]" />
      <div className="animate-pulse-glow absolute right-1/3 top-1/2 h-[300px] w-[300px] rounded-full bg-[var(--neon-cyan)] opacity-[0.05] blur-[100px]" />

      <div className="relative z-10 mx-auto grid w-full max-w-7xl items-center gap-12 px-6 md:grid-cols-2 md:px-12">
        <div className="flex flex-col gap-6">
          <div className="hero-title">
            <span className="mb-6 inline-block rounded-full border border-primary/25 bg-primary/15 px-4 py-1.5 text-xs font-medium text-[var(--neon-cyan)]">
              #1 Digital Banking Platform
            </span>
            <h1 className="text-4xl font-extrabold leading-[1.1] tracking-tight text-white sm:text-5xl lg:text-6xl">
              Next Generation{" "}
              <span className="animate-gradient-shift bg-gradient-to-r from-[var(--neon-blue)] via-[var(--neon-cyan)] to-[var(--neon-purple)] bg-clip-text text-transparent">
                Digital Banking
              </span>{" "}
              Experience
            </h1>
          </div>

          <p className="hero-sub max-w-lg text-lg text-white/70">
            Secure. Fast. Smart banking at your fingertips. Manage your finances with
            cutting-edge technology and bank-level security.
          </p>

          <div className="hero-btns flex flex-wrap gap-4">
            <Link
              to="/signup"
              className="rounded-xl bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-purple)] px-7 py-3 font-semibold text-white shadow-[0_0_30px_oklch(0.65_0.2_260_/_30%)] transition-all hover:scale-105 hover:shadow-[0_0_50px_oklch(0.65_0.2_260_/_50%)]"
            >
              Get Started
            </Link>
            <Link
              to="/login"
              className="rounded-xl border border-[var(--glass-border)] px-7 py-3 font-semibold text-white backdrop-blur-sm transition-all hover:border-primary hover:bg-primary/10"
            >
              Explore Features
            </Link>
          </div>
        </div>

        <div className="relative hidden h-[400px] md:block md:h-[500px]">
          <div className="hero-card hero-card-1 absolute right-4 top-8 flex h-[210px] w-[340px] flex-col justify-between rounded-2xl bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] p-6 shadow-[0_20px_60px_oklch(0.65_0.2_260_/_30%)]">
            <div className="flex items-start justify-between">
              <span className="text-xs font-medium uppercase tracking-widest text-white/80">
                Bank Simulation
              </span>
              <svg
                className="h-10 w-10 text-white/90"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
              >
                <circle cx="9" cy="12" r="5.5" />
                <circle cx="15" cy="12" r="5.5" />
              </svg>
            </div>
            <div>
              <div className="mb-2 font-mono text-xs tracking-[0.3em] text-white/70">
                •••• •••• •••• 4829
              </div>
              <div className="flex items-end justify-between">
                <div>
                  <div className="text-[10px] uppercase text-white/50">Card Holder</div>
                  <div className="text-sm font-medium text-white">Alex Johnson</div>
                </div>
                <div>
                  <div className="text-[10px] uppercase text-white/50">Expires</div>
                  <div className="text-sm font-medium text-white">09/28</div>
                </div>
              </div>
            </div>
          </div>

          <div className="hero-card hero-card-2 absolute bottom-12 left-0 flex h-[190px] w-[300px] flex-col justify-between rounded-2xl border border-[var(--glass-border)] bg-gradient-to-br from-[oklch(0.3_0.05_270)] to-[oklch(0.18_0.03_270)] p-5 shadow-[0_20px_60px_oklch(0_0_0_/_40%)] backdrop-blur-xl">
            <div className="flex items-center justify-between">
              <span className="text-xs uppercase tracking-widest text-white/60">Platinum</span>
              <div className="h-6 w-8 rounded bg-gradient-to-r from-yellow-400 to-yellow-600 opacity-80" />
            </div>
            <div>
              <div className="mb-2 font-mono text-xs tracking-[0.3em] text-white/50">
                •••• •••• •••• 7153
              </div>
              <div className="text-sm font-medium text-white">$24,850.00</div>
            </div>
          </div>

          <div className="hero-card absolute left-12 top-1/2 flex items-center gap-3 rounded-xl border border-[var(--glass-border)] bg-[var(--glass)] px-4 py-2.5 shadow-lg backdrop-blur-xl">
            <div className="relative flex h-8 w-8 items-center justify-center rounded-full bg-emerald-500/20">
              <span className="animate-ping-slow absolute inset-0 rounded-full bg-emerald-500/40" />
              <svg
                className="relative h-4 w-4 text-emerald-400"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path d="M5 10l7-7m0 0l7 7m-7-7v18" />
              </svg>
            </div>
            <div>
              <div className="text-[10px] text-white/60">Monthly Growth</div>
              <div className="text-sm font-bold text-emerald-400">+24.5%</div>
            </div>
          </div>
        </div>
      </div>

      <div className="absolute bottom-8 left-1/2 flex -translate-x-1/2 animate-bounce flex-col items-center gap-2 text-white/60">
        <span className="text-xs">Scroll</span>
        <svg className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M19 14l-7 7m0 0l-7-7" />
        </svg>
      </div>
    </section>
  );
}

function TrustMarquee() {
  const items = [
    "VISA",
    "Mastercard",
    "PayPal",
    "Stripe",
    "American Express",
    "Apple Pay",
    "Google Pay",
    "SWIFT",
  ];
  const loop = [...items, ...items];

  return (
    <section className="overflow-hidden border-y border-[var(--glass-border)] bg-[var(--glass)] py-10 backdrop-blur">
      <div className="mb-6 text-center text-xs uppercase tracking-[0.3em] text-white/50">
        Trusted by leading payment networks
      </div>
      <div className="relative">
        <div className="animate-marquee flex w-max gap-16 whitespace-nowrap">
          {loop.map((item, index) => (
            <span
              key={`${item}-${index}`}
              className="text-2xl font-bold text-white/40 transition-colors hover:text-white"
            >
              {item}
            </span>
          ))}
        </div>
      </div>
    </section>
  );
}

function FeaturesSection() {
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(".feat-heading", {
        scrollTrigger: { trigger: ".feat-heading", start: "top 85%" },
        y: 40,
        opacity: 0,
        duration: 0.8,
      });

      gsap.from(".feat-card", {
        scrollTrigger: { trigger: ".feat-grid", start: "top 80%" },
        y: 60,
        opacity: 0,
        duration: 0.7,
        stagger: 0.15,
        ease: "power3.out",
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  return (
    <section id="features" ref={sectionRef} className="relative px-6 py-28 md:px-12">
      <div className="mx-auto max-w-7xl">
        <div className="feat-heading mb-16 text-center">
          <span className="mb-4 inline-block rounded-full border border-primary/25 bg-primary/15 px-4 py-1.5 text-xs font-medium text-[var(--neon-cyan)]">
            Why Choose Bank Simulation
          </span>
          <h2 className="mb-4 text-3xl font-extrabold tracking-tight text-white sm:text-4xl lg:text-5xl">
            Powerful Features for{" "}
            <span className="bg-gradient-to-r from-[var(--neon-blue)] to-[var(--neon-purple)] bg-clip-text text-transparent">
              Modern Banking
            </span>
          </h2>
          <p className="mx-auto max-w-2xl text-white/70">
            Everything you need to manage, grow, and protect your finances in one secure platform.
          </p>
        </div>

        <div className="feat-grid grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((feature) => (
            <TiltCard key={feature.title} className="feat-card">
              <div className="group relative h-full overflow-hidden rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] backdrop-blur-xl transition-colors duration-500 hover:border-primary/50 hover:shadow-[0_0_40px_oklch(0.65_0.2_260_/_25%)]">
                <div className="relative h-40 overflow-hidden">
                  <img
                    src={feature.img}
                    alt={feature.title}
                    className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
                    loading="lazy"
                  />
                  <div className="absolute inset-0 bg-gradient-to-b from-transparent to-[var(--glass)]" />
                </div>
                <div className="relative p-5" style={{ transform: "translateZ(40px)" }}>
                  <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl border border-primary/25 bg-primary/15 text-[var(--neon-cyan)]">
                    {feature.icon}
                  </div>
                  <h3 className="mb-2 text-lg font-bold text-white">{feature.title}</h3>
                  <p className="text-sm leading-relaxed text-white/70">{feature.desc}</p>
                </div>
              </div>
            </TiltCard>
          ))}
        </div>
      </div>
    </section>
  );
}

function DashboardSection() {
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(".dash-text", {
        scrollTrigger: { trigger: ".dash-text", start: "top 85%" },
        x: -60,
        opacity: 0,
        duration: 0.9,
        ease: "power3.out",
      });

      gsap.from(".dash-panel", {
        scrollTrigger: { trigger: ".dash-panel", start: "top 85%" },
        x: 60,
        opacity: 0,
        duration: 0.9,
        ease: "power3.out",
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  const transactions = [
    { name: "Netflix", amount: "-$14.99", type: "Subscription", time: "2h ago", color: "bg-red-500" },
    {
      name: "Salary Deposit",
      amount: "+$5,400.00",
      type: "Income",
      time: "1d ago",
      color: "bg-emerald-500",
    },
    { name: "Amazon", amount: "-$89.50", type: "Shopping", time: "2d ago", color: "bg-orange-500" },
    {
      name: "Transfer to Sarah",
      amount: "-$250.00",
      type: "Transfer",
      time: "3d ago",
      color: "bg-primary",
    },
  ];

  return (
    <section id="about" ref={sectionRef} className="relative overflow-hidden px-6 py-28 md:px-12">
      <div className="absolute left-1/2 top-1/2 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[var(--neon-blue)] opacity-[0.05] blur-[150px]" />

      <div className="relative mx-auto grid max-w-7xl items-center gap-16 lg:grid-cols-2">
        <div className="dash-text">
          <span className="mb-4 inline-block rounded-full border border-primary/25 bg-primary/15 px-4 py-1.5 text-xs font-medium text-[var(--neon-cyan)]">
            Smart Dashboard
          </span>
          <h2 className="mb-4 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            Your Finances,{" "}
            <span className="bg-gradient-to-r from-[var(--neon-cyan)] to-[var(--neon-blue)] bg-clip-text text-transparent">
              At a Glance
            </span>
          </h2>
          <p className="mb-8 max-w-lg text-white/70">
            A beautifully designed dashboard that puts you in control. Track spending, monitor
            investments, and manage cards from one place.
          </p>

          <div className="grid grid-cols-2 gap-4">
            {[
              { label: "Active Users", value: "2.4M+" },
              { label: "Transactions/Day", value: "10M+" },
              { label: "Countries", value: "150+" },
              { label: "Uptime", value: "99.99%" },
            ].map((stat) => (
              <div
                key={stat.label}
                className="rounded-xl border border-[var(--glass-border)] bg-[var(--glass)] p-4 backdrop-blur"
              >
                <div className="text-2xl font-extrabold text-white">{stat.value}</div>
                <div className="mt-1 text-xs text-white/60">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="dash-panel rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] p-6 shadow-[0_20px_80px_oklch(0_0_0_/_40%)] backdrop-blur-xl">
          <div className="mb-6 flex items-start justify-between">
            <div>
              <div className="mb-1 text-xs text-white/60">Total Balance</div>
              <div className="text-3xl font-extrabold text-white">$48,295.60</div>
              <div className="mt-1 flex items-center gap-1">
                <span className="text-xs font-medium text-emerald-400">↑ 12.5%</span>
                <span className="text-xs text-white/60">vs last month</span>
              </div>
            </div>
            <div className="flex gap-2">
              {[
                "1W",
                "1M",
                "1Y",
              ].map((period) => (
                <button
                  key={period}
                  className={`rounded-lg px-3 py-1 text-xs transition ${
                    period === "1M"
                      ? "bg-primary text-primary-foreground"
                      : "text-white/60 hover:text-white"
                  }`}
                >
                  {period}
                </button>
              ))}
            </div>
          </div>

          <div className="relative mb-6 h-32">
            <svg viewBox="0 0 400 100" className="h-full w-full" preserveAspectRatio="none">
              <defs>
                <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="oklch(0.65 0.2 260)" stopOpacity="0.3" />
                  <stop offset="100%" stopColor="oklch(0.65 0.2 260)" stopOpacity="0" />
                </linearGradient>
              </defs>
              <path
                d="M0,70 Q50,60 100,50 T200,35 T300,25 T400,15"
                fill="none"
                stroke="oklch(0.65 0.2 260)"
                strokeWidth="2"
              />
              <path
                d="M0,70 Q50,60 100,50 T200,35 T300,25 T400,15 L400,100 L0,100 Z"
                fill="url(#chartGrad)"
              />
            </svg>
          </div>

          <div className="mb-3 text-xs font-medium text-white/60">Recent Transactions</div>
          <div className="space-y-3">
            {transactions.map((transaction) => (
              <div
                key={transaction.name}
                className="flex items-center justify-between border-b border-[var(--glass-border)] py-2 last:border-0"
              >
                <div className="flex items-center gap-3">
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/10">
                    <div className={`h-2.5 w-2.5 rounded-full ${transaction.color}`} />
                  </div>
                  <div>
                    <div className="text-sm font-medium text-white">{transaction.name}</div>
                    <div className="text-xs text-white/60">
                      {transaction.type} · {transaction.time}
                    </div>
                  </div>
                </div>
                <div
                  className={`text-sm font-semibold ${
                    transaction.amount.startsWith("+") ? "text-emerald-400" : "text-white"
                  }`}
                >
                  {transaction.amount}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function SecuritySection() {
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(".sec-item", {
        scrollTrigger: { trigger: ".sec-grid", start: "top 80%" },
        y: 50,
        opacity: 0,
        duration: 0.7,
        stagger: 0.12,
        ease: "power3.out",
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  const securityFeatures = [
    {
      title: "256-bit Encryption",
      desc: "Military-grade AES-256 encryption secures every byte of your data in transit and at rest.",
      icon: "🔐",
    },
    {
      title: "Biometric Auth",
      desc: "Face ID, Touch ID, and fingerprint authentication for instant yet secure access.",
      icon: "👁️",
    },
    {
      title: "Real-time Monitoring",
      desc: "AI-powered fraud detection monitors every transaction 24/7 and alerts you instantly.",
      icon: "🛡️",
    },
    {
      title: "Zero-Knowledge Proof",
      desc: "We verify identity without exposing private data. Privacy-first architecture by design.",
      icon: "🔒",
    },
  ];

  return (
    <section id="security" ref={sectionRef} className="relative overflow-hidden px-6 py-28 md:px-12">
      <div className="absolute inset-0">
        <img
          src="https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=1920&q=80"
          alt="Cybersecurity background"
          className="h-full w-full object-cover opacity-10"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/85 to-black/90" />
      </div>

      <div className="relative mx-auto max-w-7xl text-center">
        <span className="mb-4 inline-block rounded-full border border-emerald-500/25 bg-emerald-500/15 px-4 py-1.5 text-xs font-medium text-emerald-400">
          Bank-Level Security
        </span>
        <h2 className="mb-4 text-3xl font-extrabold tracking-tight text-white sm:text-4xl lg:text-5xl">
          Your Money is{" "}
          <span className="bg-gradient-to-r from-emerald-400 to-[var(--neon-cyan)] bg-clip-text text-transparent">
            Always Safe
          </span>
        </h2>
        <p className="mx-auto mb-14 max-w-2xl text-white/70">
          Bank-level security with advanced encryption. We protect your finances with the same
          technology used by leading financial institutions.
        </p>

        <div className="sec-grid grid gap-6 text-left sm:grid-cols-2 lg:grid-cols-4">
          {securityFeatures.map((feature) => (
            <div
              key={feature.title}
              className="sec-item group rounded-2xl border border-[var(--glass-border)] bg-[var(--glass)] p-6 backdrop-blur-xl transition-all duration-500 hover:border-emerald-500/40 hover:shadow-[0_0_40px_oklch(0.75_0.15_180_/_15%)]"
            >
              <div className="mb-4 text-3xl">{feature.icon}</div>
              <h3 className="mb-2 text-lg font-bold text-white">{feature.title}</h3>
              <p className="text-sm leading-relaxed text-white/70">{feature.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function CTASection() {
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.from(".cta-inner", {
        scrollTrigger: { trigger: ".cta-inner", start: "top 85%" },
        y: 40,
        opacity: 0,
        duration: 0.8,
        ease: "power3.out",
      });
    }, sectionRef);

    return () => ctx.revert();
  }, []);

  return (
    <section ref={sectionRef} className="px-6 py-28 md:px-12">
      <div className="cta-inner relative mx-auto max-w-4xl overflow-hidden rounded-3xl bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] p-12 text-center shadow-[0_0_80px_oklch(0.65_0.2_260_/_30%)] md:p-16">
        <div className="absolute inset-0 opacity-20">
          <div className="absolute left-0 top-0 h-full w-full bg-[radial-gradient(circle_at_30%_20%,white_0%,transparent_50%)]" />
        </div>

        <div className="relative">
          <h2 className="mb-4 text-3xl font-extrabold text-white sm:text-4xl">
            Ready to Transform Your Banking?
          </h2>
          <p className="mx-auto mb-8 max-w-lg text-white/75">
            Join users who trust Bank Simulation for daily finances. Open your account in under 5 minutes.
          </p>

          <div className="flex flex-wrap justify-center gap-4">
            <Link
              to="/signup"
              className="rounded-xl bg-white px-8 py-3.5 font-semibold text-[oklch(0.2_0.05_270)] transition-all hover:scale-105 hover:shadow-[0_8px_30px_rgba(255,255,255,0.3)]"
            >
              Open Free Account
            </Link>
            <Link
              to="/login"
              className="rounded-xl border-2 border-white/30 px-8 py-3.5 font-semibold text-white transition-all hover:border-white/60 hover:bg-white/10"
            >
              Contact Sales
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}

function Footer() {
  const columns = [
    { title: "Product", links: ["Features", "Security", "Pricing", "API"] },
    { title: "Company", links: ["About", "Careers", "Blog", "Press"] },
    { title: "Support", links: ["Help Center", "Contact", "Status", "Terms"] },
  ];

  return (
    <footer className="border-t border-[var(--glass-border)] px-6 py-16 md:px-12">
      <div className="mx-auto grid max-w-7xl gap-12 md:grid-cols-5">
        <div className="md:col-span-2">
          <div className="mb-4 flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-[var(--neon-blue)] to-[var(--neon-purple)] text-xs font-black text-white">
              B
            </div>
            <span className="text-lg font-bold tracking-tight text-white">
              Bank <span className="text-[var(--neon-cyan)]">Simulation</span>
            </span>
          </div>
          <p className="max-w-xs text-sm leading-relaxed text-white/70">
            Next-generation digital banking for the modern world. Secure, fast, and beautifully
            simple.
          </p>
        </div>

        {columns.map((column) => (
          <div key={column.title}>
            <div className="mb-4 text-sm font-semibold text-white">{column.title}</div>
            <ul className="space-y-2.5">
              {column.links.map((item) => (
                <li key={item}>
                  <button className="text-sm text-white/70 transition-colors hover:text-white">
                    {item}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="mx-auto mt-12 flex max-w-7xl flex-col items-center justify-between gap-4 border-t border-[var(--glass-border)] pt-8 md:flex-row">
        <div className="text-xs text-white/60">© 2026 Bank Simulation. All rights reserved.</div>
        <div className="flex gap-6 text-xs text-white/60">
          <button className="transition-colors hover:text-white">Privacy</button>
          <button className="transition-colors hover:text-white">Terms</button>
          <button className="transition-colors hover:text-white">Cookies</button>
        </div>
      </div>
    </footer>
  );
}

export default function Home() {
  return (
    <div
      className="nova-landing min-h-screen overflow-x-hidden"
      style={{ backgroundColor: "oklch(0.09 0.02 270)" }}
    >
      <CustomCursor />
      <Navbar />
      <HeroSection />
      <TrustMarquee />
      <FeaturesSection />
      <DashboardSection />
      <SecuritySection />
      <CTASection />
      <Footer />
    </div>
  );
}
