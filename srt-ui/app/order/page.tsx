'use client';

import { useState } from 'react';
import { useCart } from '@/lib/cartContext';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

export default function OrderPage() {
    const { items, updateQuantity, removeItem, total, totalItems, clearCart } = useCart();
    const router = useRouter();
    const [step, setStep] = useState<'cart' | 'payment' | 'receipt'>('cart');
    const [form, setForm] = useState({ name: '', email: '', card: '', expiry: '', cvv: '' });
    const [orderNumber] = useState(() => 'SRT-' + Math.floor(Math.random() * 9000 + 1000));
    const tax = total * 0.08;
    const grandTotal = total + tax;

    const handlePayment = (e: React.FormEvent) => {
        e.preventDefault();
        setStep('receipt');
        clearCart();
    };

    const handlePrint = () => window.print();

    if (items.length === 0 && step === 'cart') {
        return (
            <div style={{ minHeight: '100vh', background: '#FFF8F0', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px' }}>
                <div style={{ fontSize: '64px' }}>🛒</div>
                <h2 style={{ fontSize: '24px', fontWeight: 700, color: '#1A1A1A' }}>Your cart is empty</h2>
                <p style={{ color: '#9ca3af' }}>Add some delicious items from our menu</p>
                <Link href="/menu" style={{ background: '#C0392B', color: '#fff', padding: '12px 28px', borderRadius: '20px', fontWeight: 700, textDecoration: 'none', fontSize: '15px' }}>
                    Browse Menu
                </Link>
            </div>
        );
    }

    const inputStyle: React.CSSProperties = {
        width: '100%',
        padding: '12px 14px',
        borderRadius: '8px',
        border: '1px solid #e5e7eb',
        fontSize: '14px',
        outline: 'none',
        color: '#1A1A1A',
        background: '#fff',
        boxSizing: 'border-box',
    };

    return (
        <div style={{ minHeight: '100vh', background: '#FFF8F0', paddingTop: '80px' }}>

            {/* Header */}
            <div style={{ background: '#1A1A1A', padding: '20px', textAlign: 'center' }}>
                <h1 style={{ color: '#fff', fontSize: '28px', fontWeight: 800, margin: 0 }}>Order Online</h1>
                <p style={{ color: 'rgba(255,255,255,0.5)', fontSize: '13px', marginTop: '4px' }}>
                    Pickup ready in 20-30 minutes
                </p>
            </div>

            {/* Step indicator */}
            <div style={{ background: '#fff', borderBottom: '0.5px solid #e5e7eb', display: 'flex', justifyContent: 'center', gap: '0' }}>
                {['cart', 'payment', 'receipt'].map((s, i) => (
                    <div key={s} style={{ padding: '14px 24px', fontSize: '13px', fontWeight: 600, color: step === s ? '#C0392B' : '#9ca3af', borderBottom: step === s ? '2px solid #C0392B' : '2px solid transparent' }}>
                        {i + 1}. {s.charAt(0).toUpperCase() + s.slice(1)}
                    </div>
                ))}
            </div>

            <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1rem' }}>

                {/* CART STEP */}
                {step === 'cart' && (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '1.5rem' }}>

                        {/* Items */}
                        <div>
                            <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#1A1A1A', marginBottom: '1rem' }}>
                                Your Items ({totalItems})
                            </h2>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                {items.map(item => (
                                    <div key={item.id} style={{ background: '#fff', borderRadius: '12px', padding: '14px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', border: '0.5px solid #e5e7eb' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                            <div style={{ width: '48px', height: '48px', background: 'rgba(192,57,43,0.08)', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '22px' }}>
                                                🍽️
                                            </div>
                                            <div>
                                                <div style={{ fontWeight: 700, color: '#1A1A1A', fontSize: '14px' }}>{item.name}</div>
                                                <div style={{ color: '#C0392B', fontWeight: 600, fontSize: '13px' }}>${item.price.toFixed(2)} each</div>
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <button onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                                    style={{ width: '28px', height: '28px', borderRadius: '6px', border: '1px solid #e5e7eb', background: '#f9fafb', cursor: 'pointer', fontSize: '16px', fontWeight: 700, color: '#1A1A1A', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                                    −
                                                </button>
                                                <span style={{ fontWeight: 700, fontSize: '14px', minWidth: '20px', textAlign: 'center', color: '#1A1A1A' }}>{item.quantity}</span>
                                                <button onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                                    style={{ width: '28px', height: '28px', borderRadius: '6px', border: '1px solid #e5e7eb', background: '#f9fafb', cursor: 'pointer', fontSize: '16px', fontWeight: 700, color: '#1A1A1A', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                                    +
                                                </button>
                                            </div>
                                            <div style={{ fontWeight: 700, color: '#1A1A1A', fontSize: '14px', minWidth: '55px', textAlign: 'right' }}>
                                                ${(item.price * item.quantity).toFixed(2)}
                                            </div>
                                            <button onClick={() => removeItem(item.id)}
                                                style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: '16px', padding: '4px' }}>
                                                ✕
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            <Link href="/menu" style={{ display: 'inline-block', marginTop: '16px', color: '#C0392B', fontWeight: 600, fontSize: '14px', textDecoration: 'none' }}>
                                + Add more items
                            </Link>
                        </div>

                        {/* Order summary */}
                        <div>
                            <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', border: '0.5px solid #e5e7eb', position: 'sticky', top: '100px' }}>
                                <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#1A1A1A', marginBottom: '16px' }}>Order Summary</h3>
                                {items.map(item => (
                                    <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '8px' }}>
                                        <span>{item.name} × {item.quantity}</span>
                                        <span>${(item.price * item.quantity).toFixed(2)}</span>
                                    </div>
                                ))}
                                <div style={{ borderTop: '0.5px solid #e5e7eb', marginTop: '12px', paddingTop: '12px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '6px' }}>
                                        <span>Subtotal</span><span>${total.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '12px' }}>
                                        <span>Tax (8%)</span><span>${tax.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '16px', fontWeight: 700, color: '#1A1A1A' }}>
                                        <span>Total</span><span style={{ color: '#C0392B' }}>${grandTotal.toFixed(2)}</span>
                                    </div>
                                </div>
                                <button onClick={() => setStep('payment')}
                                    style={{ width: '100%', background: '#C0392B', color: '#fff', border: 'none', borderRadius: '10px', padding: '14px', fontSize: '15px', fontWeight: 700, cursor: 'pointer', marginTop: '16px' }}>
                                    Proceed to Payment →
                                </button>
                                <p style={{ fontSize: '11px', color: '#9ca3af', textAlign: 'center', marginTop: '10px' }}>
                                    🔒 Secure checkout
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* PAYMENT STEP */}
                {step === 'payment' && (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '1.5rem' }}>
                        <div>
                            <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#1A1A1A', marginBottom: '1.5rem' }}>Payment Details</h2>
                            <form onSubmit={handlePayment} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', border: '0.5px solid #e5e7eb' }}>
                                    <h3 style={{ fontSize: '14px', fontWeight: 700, color: '#1A1A1A', marginBottom: '16px' }}>Contact Information</h3>
                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                        <div>
                                            <label style={{ fontSize: '12px', color: '#6b7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>Full Name</label>
                                            <input style={inputStyle} placeholder="John Smith" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
                                        </div>
                                        <div>
                                            <label style={{ fontSize: '12px', color: '#6b7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>Email</label>
                                            <input style={inputStyle} type="email" placeholder="john@email.com" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
                                        </div>
                                    </div>
                                </div>

                                <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', border: '0.5px solid #e5e7eb' }}>
                                    <h3 style={{ fontSize: '14px', fontWeight: 700, color: '#1A1A1A', marginBottom: '16px' }}>Card Details</h3>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                        <div>
                                            <label style={{ fontSize: '12px', color: '#6b7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>Card Number</label>
                                            <input style={inputStyle} placeholder="1234 5678 9012 3456" value={form.card}
                                                onChange={e => setForm({ ...form, card: e.target.value.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim() })}
                                                maxLength={19} required />
                                        </div>
                                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                            <div>
                                                <label style={{ fontSize: '12px', color: '#6b7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>Expiry Date</label>
                                                <input style={inputStyle} placeholder="MM/YY" value={form.expiry}
                                                    onChange={e => {
                                                        let v = e.target.value.replace(/\D/g, '').slice(0, 4);
                                                        if (v.length >= 2) v = v.slice(0, 2) + '/' + v.slice(2);
                                                        setForm({ ...form, expiry: v });
                                                    }}
                                                    maxLength={5} required />
                                            </div>
                                            <div>
                                                <label style={{ fontSize: '12px', color: '#6b7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>CVV</label>
                                                <input style={inputStyle} placeholder="123" value={form.cvv}
                                                    onChange={e => setForm({ ...form, cvv: e.target.value.replace(/\D/g, '').slice(0, 3) })}
                                                    maxLength={3} required />
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{ marginTop: '12px', background: '#fef9c3', borderRadius: '8px', padding: '10px 12px', fontSize: '12px', color: '#854d0e' }}>
                                        💡 This is a demo — use any card number
                                    </div>
                                </div>

                                <div style={{ display: 'flex', gap: '12px' }}>
                                    <button type="button" onClick={() => setStep('cart')}
                                        style={{ flex: 1, background: '#f3f4f6', color: '#6b7280', border: 'none', borderRadius: '10px', padding: '14px', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}>
                                        ← Back
                                    </button>
                                    <button type="submit"
                                        style={{ flex: 2, background: '#C0392B', color: '#fff', border: 'none', borderRadius: '10px', padding: '14px', fontSize: '15px', fontWeight: 700, cursor: 'pointer' }}>
                                        Pay ${grandTotal.toFixed(2)} →
                                    </button>
                                </div>
                            </form>
                        </div>

                        {/* Summary */}
                        <div>
                            <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', border: '0.5px solid #e5e7eb', position: 'sticky', top: '100px' }}>
                                <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#1A1A1A', marginBottom: '16px' }}>Order Summary</h3>
                                {items.map(item => (
                                    <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '8px' }}>
                                        <span>{item.name} × {item.quantity}</span>
                                        <span>${(item.price * item.quantity).toFixed(2)}</span>
                                    </div>
                                ))}
                                <div style={{ borderTop: '0.5px solid #e5e7eb', marginTop: '12px', paddingTop: '12px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '6px' }}>
                                        <span>Subtotal</span><span>${total.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '12px' }}>
                                        <span>Tax (8%)</span><span>${tax.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '16px', fontWeight: 700, color: '#1A1A1A' }}>
                                        <span>Total</span><span style={{ color: '#C0392B' }}>${grandTotal.toFixed(2)}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* RECEIPT STEP */}
                {step === 'receipt' && (
                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        <div id="receipt" style={{ background: '#fff', borderRadius: '16px', padding: '32px', maxWidth: '420px', width: '100%', border: '0.5px solid #e5e7eb' }}>
                            <div style={{ textAlign: 'center', marginBottom: '24px', paddingBottom: '20px', borderBottom: '1px dashed #e5e7eb' }}>
                                <div style={{ fontSize: '40px', marginBottom: '8px' }}>🌶</div>
                                <h2 style={{ fontSize: '20px', fontWeight: 800, color: '#1A1A1A', margin: 0 }}>Spice Road Truck</h2>
                                <p style={{ color: '#9ca3af', fontSize: '13px', margin: '4px 0 0' }}>Atlanta, GA · Thank you for your order!</p>
                                <div style={{ marginTop: '12px', background: '#f0fdf4', borderRadius: '8px', padding: '8px 16px', display: 'inline-block' }}>
                                    <span style={{ fontSize: '13px', fontWeight: 700, color: '#16a34a' }}>✓ Order Confirmed</span>
                                </div>
                                <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '8px' }}>
                                    Order #{orderNumber} · {new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                                </p>
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                {JSON.parse(localStorage.getItem('srt-last-order') || '[]').map((item: any, i: number) => (
                                    <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: '#1A1A1A', padding: '6px 0' }}>
                                        <span>{item.name} × {item.quantity}</span>
                                        <span>${(item.price * item.quantity).toFixed(2)}</span>
                                    </div>
                                ))}
                            </div>

                            <hr style={{ border: 'none', borderTop: '1px dashed #e5e7eb', margin: '12px 0' }} />

                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '6px' }}>
                                <span>Subtotal</span><span>${total.toFixed(2)}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#6b7280', marginBottom: '12px' }}>
                                <span>Tax (8%)</span><span>${tax.toFixed(2)}</span>
                            </div>

                            <hr style={{ border: 'none', borderTop: '1px dashed #e5e7eb', margin: '12px 0' }} />

                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '18px', fontWeight: 800, color: '#1A1A1A' }}>
                                <span>Total</span><span style={{ color: '#C0392B' }}>${grandTotal.toFixed(2)}</span>
                            </div>

                            <div style={{ marginTop: '16px', background: '#f9fafb', borderRadius: '8px', padding: '12px', fontSize: '13px', color: '#6b7280', textAlign: 'center' }}>
                                💳 {form.card ? `Visa ending in ${form.card.replace(/\s/g, '').slice(-4)}` : 'Demo payment'}
                            </div>

                            <div style={{ marginTop: '24px', display: 'flex', gap: '12px' }}>
                                <button onClick={handlePrint}
                                    style={{ flex: 1, background: '#1A1A1A', color: '#fff', border: 'none', borderRadius: '10px', padding: '12px', fontSize: '14px', fontWeight: 700, cursor: 'pointer' }}>
                                    🖨 Print Receipt
                                </button>
                                <Link href="/menu"
                                    style={{ flex: 1, background: '#C0392B', color: '#fff', border: 'none', borderRadius: '10px', padding: '12px', fontSize: '14px', fontWeight: 700, cursor: 'pointer', textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    Order More
                                </Link>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}