'use client';
import Link from 'next/link';
import { Phone, Mail, ArrowLeft } from 'lucide-react';

interface ComingSoonProps {
  page: string;
  emoji: string;
  message: string;
}

export default function ComingSoon({ page, emoji, message }: ComingSoonProps) {
  return (
    <div className="min-h-screen bg-[#1A1A1A] flex flex-col items-center justify-center overflow-hidden relative px-4">

      {/* Background glow */}
      <div
        className="absolute inset-0 opacity-10"
        style={{
          backgroundImage: `radial-gradient(circle at 50% 60%, #C0392B 0%, transparent 60%)`,
        }}
      />

      {/* Road */}
      <div className="absolute bottom-0 left-0 right-0 h-20 bg-[#111]">
        <div className="absolute top-0 left-0 right-0 h-1 bg-[#C0392B] opacity-40" />
        {/* Road dashes */}
        <div className="absolute top-1/2 -translate-y-1/2 left-0 right-0 flex gap-8 px-4 overflow-hidden">
          {Array.from({ length: 20 }).map((_, i) => (
            <div
              key={i}
              className="h-2 w-16 bg-[#333] rounded flex-shrink-0"
              style={{
                animation: 'roadDash 1.5s linear infinite',
                animationDelay: `${i * -0.15}s`,
              }}
            />
          ))}
        </div>
      </div>

      {/* Animated truck */}
      <div
        className="absolute bottom-10"
        style={{ animation: 'truckDrive 8s linear infinite' }}
      >
        <div className="relative">

          {/* Exhaust smoke puffs */}
          <div className="absolute -left-4 top-4 flex flex-col gap-1">
            <div
              className="w-3 h-3 rounded-full bg-gray-400 opacity-50"
              style={{ animation: 'smoke 1.5s ease-out infinite', animationDelay: '0s' }}
            />
            <div
              className="w-4 h-4 rounded-full bg-gray-400 opacity-35"
              style={{ animation: 'smoke 1.5s ease-out infinite', animationDelay: '0.3s' }}
            />
            <div
              className="w-5 h-5 rounded-full bg-gray-400 opacity-20"
              style={{ animation: 'smoke 1.5s ease-out infinite', animationDelay: '0.6s' }}
            />
          </div>

          {/* Real truck image — flipped to face right */}
          <img
            src="/images/hero/truck.png"
            alt="Spice Road Truck"
            className="w-52 object-contain"
            style={{
              transform: 'scaleX(-1)',
              filter: 'drop-shadow(0 8px 16px rgba(192,57,43,0.4))',
            }}
          />

        </div>
      </div>

      {/* Content */}
      <div className="relative z-10 text-center max-w-2xl mx-auto mb-32">

        {/* Back link */}
        <Link
          href="/"
          className="inline-flex items-center gap-2 text-gray-400 hover:text-[#E67E22] transition-colors mb-10 text-sm"
        >
          <ArrowLeft size={16} /> Back to Home
        </Link>

        {/* Emoji */}
        <div className="text-7xl mb-6">{emoji}</div>

        {/* Coming soon badge */}
        <div className="inline-flex items-center gap-2 bg-[#C0392B]/20 border border-[#C0392B]/40 rounded-full px-4 py-1.5 mb-6">
          <span className="w-2 h-2 rounded-full bg-[#C0392B] animate-pulse" />
          <span className="text-[#E67E22] text-sm font-semibold uppercase tracking-widest">
            Coming Soon
          </span>
        </div>

        <h1 className="text-5xl lg:text-6xl font-black text-white mb-4">
          {page}
        </h1>

        <p className="text-gray-400 text-lg leading-relaxed mb-10 max-w-lg mx-auto">
          {message}
        </p>

        {/* Contact options */}
        <p className="text-gray-500 text-sm mb-4 uppercase tracking-widest">Reach us directly</p>
        <div className="flex flex-wrap gap-4 justify-center">
          <a
            href="tel:+14699331897"
            className="flex items-center gap-2 bg-[#C0392B] hover:bg-[#E67E22] text-white px-6 py-3 rounded-full font-semibold transition-colors"
          >
            <Phone size={18} /> Call Us
          </a>
          <a
            href="mailto:contact@spiceroadtruck.com"
            className="flex items-center gap-2 border border-white/20 hover:border-[#E67E22] text-white hover:text-[#E67E22] px-6 py-3 rounded-full font-semibold transition-colors"
          >
            <Mail size={18} /> Email Us
          </a>
        </div>

      </div>

      {/* CSS animations */}
      <style>{`
        @keyframes truckDrive {
          0%   { left: -250px; }
          100% { left: 110%; }
        }
        @keyframes smoke {
          0%   { transform: translateY(0) scale(1); opacity: 0.5; }
          100% { transform: translateY(-24px) scale(2.5); opacity: 0; }
        }
        @keyframes roadDash {
          0%   { transform: translateX(0); }
          100% { transform: translateX(-200px); }
        }
      `}</style>

    </div>
  );
}