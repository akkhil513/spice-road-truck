'use client';
import { useState } from 'react';
import Link from 'next/link';
import { Menu, X } from 'lucide-react';

export default function Navbar() {
    const [isOpen, setIsOpen] = useState(false);

    const links = [
        { href: '/', label: 'Home' },
        { href: '/menu', label: 'Menu' },
        { href: '/order', label: 'Order Online' },
        { href: '/catering', label: 'Catering' },
        { href: '/contact', label: 'Contact' },
    ];

    return (
        <nav className="fixed top-0 left-0 right-0 z-50 bg-[#1A1A1A] shadow-lg">
            <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
                <Link href="/" className="flex items-center gap-2">
                    <img
                        src="/images/logo/srt-logo-dark.svg"
                        alt="Spice Road Truck"
                        width={44}
                        height={44}
                        className="rounded-lg"
                    />
                    <span className="text-white font-bold text-lg hidden sm:block">
                        Spice Road Truck
                    </span>
                </Link>

                <div className="hidden md:flex items-center gap-6">
                    {links.map((link) => (
                        <Link key={link.href} href={link.href}
                            className="text-gray-300 hover:text-[#E67E22] transition-colors duration-200 font-medium">
                            {link.label}
                        </Link>
                    ))}
                    <Link href="/order"
                        className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-4 py-2 rounded-full font-semibold transition-colors duration-200">
                        Order Now
                    </Link>
                </div>

                <button className="md:hidden text-white" onClick={() => setIsOpen(!isOpen)}>
                    {isOpen ? <X size={24} /> : <Menu size={24} />}
                </button>
            </div>

            {isOpen && (
                <div className="md:hidden bg-[#1A1A1A] border-t border-gray-700 px-4 py-4 flex flex-col gap-4">
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
                </div>
            )}
        </nav>
    );
}