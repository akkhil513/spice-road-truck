'use client';

import { useEffect, useState, useRef } from 'react';
import Link from 'next/link';
import { Menu, X } from 'lucide-react';
import { useAuth } from '@/lib/authContext';
import { useRouter } from 'next/navigation';

export default function Navbar() {
    const [isOpen, setIsOpen] = useState(false);
    const [visitors, setVisitors] = useState<number>(0);
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const { isAdmin, logout, userName } = useAuth();
    const router = useRouter();
    const dropdownRef = useRef<HTMLDivElement>(null);

    const links = [
        { href: '/', label: 'Home' },
        { href: '/menu', label: 'Menu' },
        { href: '/order', label: 'Order Online' },
        { href: '/catering', label: 'Catering' },
        { href: '/contact', label: 'Contact' },
    ];

    useEffect(() => {
        let isMounted = true;
        fetch('https://api.counterapi.dev/v1/spiceroadtruck/visits/up')
            .then((res) => res.json())
            .then((data) => {
                if (isMounted) setVisitors(typeof data.count === 'number' ? data.count : 0);
            })
            .catch(() => { if (isMounted) setVisitors(0); });
        return () => { isMounted = false; };
    }, []);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = async () => {
        setDropdownOpen(false);
        await logout();
        router.push('/');
    };

    // Get initials from email or name
    const getInitials = (name: string) => {
        if (!name) return 'A';
        // If it's an email, take first letter before @
        if (name.includes('@')) return name[0].toUpperCase();
        return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    };

    return (
        <nav className="fixed top-0 left-0 right-0 z-50">

            {/* Top bar */}
            <div className="bg-[#C0392B] py-1 px-4 flex justify-center items-center gap-4">
                <span className="text-white text-[11px] tracking-wide">
                    👥 {visitors.toLocaleString()} visitors
                </span>
                <span className="text-white/40 text-[11px]">|</span>
                <span className="text-white text-[11px] tracking-wide">
                    🌶 Now Serving Atlanta
                </span>
            </div>

            {/* Main navbar */}
            <div className="bg-[#1A1A1A] shadow-lg">
                <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
                    <Link href="/" className="flex items-center gap-2">
                        <img src="/images/logo/srt-logo-dark.svg" alt="Spice Road Truck" width={44} height={44} className="rounded-lg" />
                        <span className="text-white font-bold text-lg hidden sm:block">Spice Road Truck</span>
                    </Link>

                    {/* Desktop */}
                    <div className="hidden md:flex items-center gap-3">
                        {links.map((link) => (
                            <Link key={link.href} href={link.href}
                                className="text-gray-300 hover:text-[#E67E22] transition-colors duration-200 font-medium px-2">
                                {link.label}
                            </Link>
                        ))}

                        <Link href="/order"
                            className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-4 py-2 rounded-full font-semibold transition-colors duration-200">
                            Order Now
                        </Link>

                        {/* Admin avatar dropdown or Admin button */}
                        {isAdmin ? (
                            <div ref={dropdownRef} style={{ position: 'relative' }}>
                                <button
                                    onClick={() => setDropdownOpen(!dropdownOpen)}
                                    style={{
                                        width: '36px', height: '36px', borderRadius: '50%',
                                        background: '#C0392B', color: '#fff', border: '2px solid rgba(255,255,255,0.2)',
                                        cursor: 'pointer', fontSize: '13px', fontWeight: 700,
                                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                                        transition: 'border-color 0.2s',
                                    }}>
                                    {getInitials(userName)}
                                </button>

                                {dropdownOpen && (
                                    <div style={{
                                        position: 'absolute', top: '44px', right: 0,
                                        background: '#2A2A2A', border: '1px solid rgba(255,255,255,0.1)',
                                        borderRadius: '10px', padding: '6px', minWidth: '180px',
                                        boxShadow: '0 8px 24px rgba(0,0,0,0.4)', zIndex: 100,
                                    }}>
                                        {/* User info */}
                                        <div style={{ padding: '8px 12px', borderBottom: '1px solid rgba(255,255,255,0.08)', marginBottom: '4px' }}>
                                            <div style={{ fontSize: '12px', fontWeight: 600, color: '#fff' }}>
                                                {userName.includes('@') ? userName.split('@')[0] : userName}
                                            </div>
                                            <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)' }}>Administrator</div>
                                        </div>

                                        <Link href="/admin/dashboard"
                                            onClick={() => setDropdownOpen(false)}
                                            style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', color: '#fff', textDecoration: 'none', fontSize: '13px', borderRadius: '6px', transition: 'background 0.15s' }}
                                            onMouseEnter={e => (e.currentTarget.style.background = 'rgba(255,255,255,0.06)')}
                                            onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}>
                                            📊 Dashboard
                                        </Link>

                                        <button
                                            onClick={handleLogout}
                                            style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', color: '#E74C3C', background: 'none', border: 'none', width: '100%', textAlign: 'left', fontSize: '13px', cursor: 'pointer', borderRadius: '6px', transition: 'background 0.15s' }}
                                            onMouseEnter={e => (e.currentTarget.style.background = 'rgba(231,76,60,0.1)')}
                                            onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}>
                                            🚪 Logout
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <Link href="/admin"
                                className="text-gray-500 hover:text-white border border-gray-700 hover:border-gray-500 px-3 py-2 rounded-full text-sm transition-colors duration-200">
                                Admin
                            </Link>
                        )}
                    </div>

                    <button className="md:hidden text-white" onClick={() => setIsOpen(!isOpen)}>
                        {isOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>
                </div>

                {/* Mobile menu */}
                {isOpen && (
                    <div className="md:hidden border-t border-gray-700 px-4 py-4 flex flex-col gap-4">
                        {links.map((link) => (
                            <Link key={link.href} href={link.href}
                                className="text-gray-300 hover:text-[#E67E22] transition-colors font-medium py-1"
                                onClick={() => setIsOpen(false)}>
                                {link.label}
                            </Link>
                        ))}
                        <Link href="/order"
                            className="bg-[#C0392B] text-white px-4 py-2 rounded-full font-semibold text-center"
                            onClick={() => setIsOpen(false)}>
                            Order Now
                        </Link>
                        {isAdmin ? (
                            <>
                                <Link href="/admin/dashboard"
                                    className="text-gray-300 font-medium py-1"
                                    onClick={() => setIsOpen(false)}>
                                    📊 Dashboard
                                </Link>
                                <button onClick={handleLogout}
                                    className="text-left text-red-400 font-medium py-1 bg-none border-none cursor-pointer">
                                    🚪 Logout
                                </button>
                            </>
                        ) : (
                            <Link href="/admin"
                                className="text-gray-400 border border-gray-700 px-4 py-2 rounded-full text-sm text-center"
                                onClick={() => setIsOpen(false)}>
                                Admin
                            </Link>
                        )}
                    </div>
                )}
            </div>
        </nav>
    );
}