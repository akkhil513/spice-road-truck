'use client';
import { useState } from 'react';
import emailjs from '@emailjs/browser';
import Link from 'next/link';
import { Phone, Mail, MapPin, Instagram, Facebook, CheckCircle } from 'lucide-react';

export default function Footer() {
    const [email, setEmail] = useState('');
    const [subscribed, setSubscribed] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const getEmailJsErrorText = (err: unknown): string => {
        const emailJsError = err as { status?: number; text?: string; message?: string };
        if (emailJsError?.text) return emailJsError.text;
        if (emailJsError?.message) return emailJsError.message;
        return 'Unknown EmailJS error';
    };

    const handleSubscribe = async () => {
        if (!email || !email.includes('@')) {
            setError('Please enter a valid email.');
            return;
        }
        setError('');
        setLoading(true);

        try {
            // 1. Notify admin (optional)
            const adminTemplateId = process.env.NEXT_PUBLIC_EMAILJS_ADMIN_TEMPLATE_ID;
            if (adminTemplateId) {
                try {
                    await emailjs.send(
                        process.env.NEXT_PUBLIC_EMAILJS_SERVICE_ID!,
                        adminTemplateId,
                        {
                            subscriber_email: email,
                            date: new Date().toLocaleDateString(),
                        },
                        process.env.NEXT_PUBLIC_EMAILJS_PUBLIC_KEY!
                    );
                } catch (err: unknown) {
                    const errorText = getEmailJsErrorText(err);
                    console.error('EmailJS admin template failed:', adminTemplateId);
                    console.error('EmailJS admin error:', errorText);
                }
            }

            // 2. Send welcome email to subscriber
            try {
                await emailjs.send(
                    process.env.NEXT_PUBLIC_EMAILJS_SERVICE_ID!,
                    process.env.NEXT_PUBLIC_EMAILJS_WELCOME_TEMPLATE_ID!,
                    {
                        subscriber_email: email,
                    },
                    process.env.NEXT_PUBLIC_EMAILJS_PUBLIC_KEY!
                );
            } catch (err: unknown) {
                const errorText = getEmailJsErrorText(err);
                console.error('EmailJS welcome template failed:', process.env.NEXT_PUBLIC_EMAILJS_WELCOME_TEMPLATE_ID);
                console.error('EmailJS welcome error:', errorText);
                setError(`Welcome template failed: ${errorText}`);
                return;
            }

            setSubscribed(true);
            setEmail('');
        } catch (err: unknown) {
            const error = err as { status?: number; text?: string; message?: string };
            console.error('EmailJS error status:', error?.status);
            console.error('EmailJS error text:', error?.text);
            console.error('EmailJS error message:', error?.message);
            setError('Something went wrong. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <footer className="bg-[#1A1A1A] text-gray-300 pt-12 pb-6">
            <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 md:grid-cols-3 gap-10">

                {/* Brand */}
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
                        Bringing bold Indo-American street food fusion to Atlanta.
                        Flavor that hits different, every single time.
                    </p>
                    <div className="flex gap-4 mb-6">
                        <a
                            href="https://facebook.com"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-gray-400 hover:text-[#E67E22] transition-colors"
                        >
                            <Facebook size={20} />
                        </a>
                        <a
                            href="https://instagram.com"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-gray-400 hover:text-[#E67E22] transition-colors"
                        >
                            <Instagram size={20} />
                        </a>
                    </div>

                    {/* Newsletter */}
                    <p className="text-sm font-semibold text-white mb-2">Stay Connected</p>
                    {subscribed ? (
                        <div className="flex items-center gap-2 text-green-400 text-sm font-semibold">
                            <CheckCircle size={16} />
                            <span>Thanks for subscribing!</span>
                        </div>
                    ) : (
                        <div className="flex flex-col gap-2">
                            <div className="flex gap-2">
                                <input
                                    type="email"
                                    value={email}
                                    onChange={e => {
                                        setEmail(e.target.value);
                                        setError('');
                                    }}
                                    onKeyDown={e => e.key === 'Enter' && handleSubscribe()}
                                    placeholder="Your email"
                                    className="flex-1 bg-gray-800 text-white px-3 py-2 rounded-lg text-sm border border-gray-700 focus:outline-none focus:border-[#E67E22] transition-colors"
                                />
                                <button
                                    onClick={handleSubscribe}
                                    disabled={loading}
                                    className="bg-[#C0392B] hover:bg-[#E67E22] disabled:opacity-50 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-colors flex items-center justify-center min-w-[90px]"
                                >
                                    {loading ? (
                                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    ) : (
                                        'Subscribe'
                                    )}
                                </button>
                            </div>
                            {error && (
                                <p className="text-red-400 text-xs">{error}</p>
                            )}
                        </div>
                    )}
                </div>

                {/* Quick Links */}
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
                                <Link
                                    href={link.href}
                                    className="hover:text-[#E67E22] transition-colors"
                                >
                                    {link.label}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Contact */}
                <div>
                    <h3 className="text-white font-semibold text-lg mb-4">Contact Us</h3>
                    <ul className="space-y-3 text-sm">
                        <li className="flex items-start gap-2">
                            <MapPin size={16} className="text-[#C0392B] mt-0.5 shrink-0" />
                            <span>Atlanta, GA, USA</span>
                        </li>
                        <li className="flex items-center gap-2">
                            <Phone size={16} className="text-[#C0392B] shrink-0" />
                            <a
                                href="tel:+14699331897"
                                className="hover:text-[#E67E22] transition-colors"
                            >
                                +1 469-933-1897
                            </a>
                        </li>
                        <li className="flex items-center gap-2">
                            <Mail size={16} className="text-[#C0392B] shrink-0" />
                            <a
                                href="mailto:contact@spiceroadtruck.com"
                                className="hover:text-[#E67E22] transition-colors"
                            >
                                contact@spiceroadtruck.com
                            </a>
                        </li>
                    </ul>
                </div>

            </div>

            {/* Bottom bar */}
            <div className="max-w-7xl mx-auto px-4 mt-10 pt-6 border-t border-gray-800 flex flex-col sm:flex-row justify-between items-center gap-2 text-xs text-gray-500">
                <p>© 2025 Spice Road Truck. All rights reserved.</p>
                <p>Built with ❤️ in Atlanta, GA</p>
            </div>
        </footer>
    );
}