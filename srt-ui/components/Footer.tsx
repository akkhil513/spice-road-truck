import Link from 'next/link';
import { Phone, Mail, MapPin, Instagram, Facebook } from 'lucide-react';

export default function Footer() {
    return (
        <footer className="bg-[#1A1A1A] text-gray-300 pt-12 pb-6">
            <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-3 gap-10">

                <div>
                    <div className="flex items-center gap-2 mb-4">
                        <img
                            src="/images/logo/srt-logo-dark.svg"
                            alt="Spice Road Truck"
                            width={44}
                            height={44}
                            className="rounded-lg"
                        />
                        <span className="text-white font-bold text-lg">Spice Road Truck</span>
                    </div>
                    <p className="text-sm leading-relaxed text-gray-400 mb-4">
                        Authentic Indo-American fusion cuisine crafted on wheels.
                        Experience bold flavors and unforgettable taste in every bite.
                    </p>
                    <div className="flex gap-4 mt-4">
                        <a href="https://facebook.com" target="_blank" rel="noopener noreferrer"
                            className="text-gray-400 hover:text-[#E67E22] transition-colors">
                            <Facebook size={20} />
                        </a>
                        <a href="https://instagram.com" target="_blank" rel="noopener noreferrer"
                            className="text-gray-400 hover:text-[#E67E22] transition-colors">
                            <Instagram size={20} />
                        </a>
                    </div>
                </div>

                <div>
                    <h3 className="text-white font-semibold text-lg mb-4">Quick Links</h3>
                    <ul className="space-y-2 text-sm">
                        {[
                            { href: '/', label: 'Home' },
                            { href: '/menu', label: 'Menu' },
                            { href: '/order', label: 'Order Online' },
                            { href: '/catering', label: 'Catering' },
                            { href: '/contact', label: 'Contact' },
                        ].map((link) => (
                            <li key={link.href}>
                                <Link href={link.href} className="hover:text-[#E67E22] transition-colors">
                                    {link.label}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>

                <div>
                    <h3 className="text-white font-semibold text-lg mb-4">Contact Us</h3>
                    <ul className="space-y-3 text-sm">
                        <li className="flex items-start gap-2">
                            <MapPin size={16} className="text-[#C0392B] mt-0.5 shrink-0" />
                            <span>Atlanta, GA, USA</span>
                        </li>
                        <li className="flex items-center gap-2">
                            <Phone size={16} className="text-[#C0392B] shrink-0" />
                            <a href="tel:+14693331897" className="hover:text-[#E67E22] transition-colors">
                                +1 469-333-1897
                            </a>
                        </li>
                        <li className="flex items-center gap-2">
                            <Mail size={16} className="text-[#C0392B] shrink-0" />
                            <a href="mailto:akhilgollapalli@spiceroadtruck.com"
                                className="hover:text-[#E67E22] transition-colors">
                                akhilgollapalli@spiceroadtruck.com
                            </a>
                        </li>
                    </ul>
                    <div className="mt-6">
                        <p className="text-sm font-semibold text-white mb-2">Stay Connected</p>
                        <div className="flex gap-2">
                            <input type="email" placeholder="Your email"
                                className="flex-1 bg-gray-800 text-white px-3 py-2 rounded-lg text-sm border border-gray-700 focus:outline-none focus:border-[#E67E22]" />
                            <button className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-4 py-2 rounded-lg text-sm font-semibold transition-colors">
                                Subscribe
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-4 mt-10 pt-6 border-t border-gray-800 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-gray-500">
                <p>© 2026 Spice Road Truck. All rights reserved.</p>
                <p>Built with ❤️ in Atlanta, GA</p>
            </div>
        </footer>
    );
}