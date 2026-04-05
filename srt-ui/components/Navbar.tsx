'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Menu, X } from 'lucide-react';
import { useAuth } from '@/lib/authContext';
import { useRouter } from 'next/navigation';

export default function Navbar() {
    const [isOpen, setIsOpen] = useState(false);
    const [visitors, setVisitors] = useState<number>(0);
    const { isAdmin, logout } = useAuth();
    const router = useRouter();

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

    const handleLogout = async () => {
        await logout();
        router.push('/');
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

                        {/* Order Now — always visible */}
                        <Link href="/order"
                            className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-4 py-2 rounded-full font-semibold transition-colors duration-200">
                            Order Now
                        </Link>

                        {/* Admin or Logout */}
                        {isAdmin ? (
                            <button onClick={handleLogout}
                                className="bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-full font-semibold transition-colors duration-200 text-sm">
                                Logout
                            </button>
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
                            <button onClick={handleLogout}
                                className="bg-gray-700 text-white px-4 py-2 rounded-full font-semibold text-center">
                                Logout
                            </button>
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