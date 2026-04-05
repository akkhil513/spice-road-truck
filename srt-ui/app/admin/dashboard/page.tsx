'use client';

import { useEffect, useState } from 'react';
import { signOut, fetchAuthSession } from 'aws-amplify/auth';
import '../../../lib/auth';
import { useRouter } from 'next/navigation';

interface Dish {
    id: string;
    name: string;
    description: string;
    price: number;
    category: string;
    imageUrl: string;
}

interface OrderItem {
    id: string;
    name: string;
    price: number;
    quantity: number;
}

interface Order {
    pk: string;
    sk: string;
    orderNumber: string;
    customerName: string;
    customerEmail: string;
    cardLast4: string;
    items: OrderItem[];
    subtotal: number;
    tax: number;
    grandTotal: number;
    orderDate: string;
    orderTime: string;
    status: string;
}

const API = 'https://api.spiceroadtruck.com';

const STATUS_COLORS: Record<string, { bg: string; color: string }> = {
    NEW: { bg: 'rgba(59,130,246,0.2)', color: '#60a5fa' },
    PREPARING: { bg: 'rgba(234,179,8,0.2)', color: '#facc15' },
    READY: { bg: 'rgba(34,197,94,0.2)', color: '#4ade80' },
    COMPLETED: { bg: 'rgba(156,163,175,0.2)', color: '#9ca3af' },
};

export default function AdminDashboard() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState<'dishes' | 'orders'>('dishes');

    // Dishes state
    const [dishes, setDishes] = useState<Dish[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editingDish, setEditingDish] = useState<Dish | null>(null);
    const [isNewCategory, setIsNewCategory] = useState(false);
    const [form, setForm] = useState({ name: '', description: '', price: '', category: '', imageUrl: '' });
    const [uniqueCategories, setUniqueCategories] = useState<string[]>([]);

    // Orders state
    const [orders, setOrders] = useState<Order[]>([]);
    const [ordersLoading, setOrdersLoading] = useState(false);
    const [filterName, setFilterName] = useState('');
    const [filterCard, setFilterCard] = useState('');
    const [filterDate, setFilterDate] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filterDateFrom, setFilterDateFrom] = useState('');
    const [filterDateTo, setFilterDateTo] = useState('');

    const [viewingOrder, setViewingOrder] = useState<Order | null>(null);

    useEffect(() => {
        checkAuthAndLoad();
    }, []);

    const checkAuthAndLoad = async () => {
        try {
            await fetchAuthSession();
            loadDishes();
            loadOrders();
        } catch {
            router.push('/admin');
        }
    };

    const loadDishes = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API}/srt/v1/dishes`);
            const data = await res.json();
            setDishes(data);
            const cats = [...new Set(data.map((d: Dish) => d.category).filter(Boolean))] as string[];
            setUniqueCategories(cats);
        } catch {
            setError('Failed to load dishes');
        } finally {
            setLoading(false);
        }
    };

    const loadOrders = async () => {
        setOrdersLoading(true);
        try {
            const res = await fetch(`${API}/srt/v1/orders`);
            const data = await res.json();
            const ordersList = Array.isArray(data) ? data : [data];
            ordersList.sort((a: Order, b: Order) => b.orderNumber?.localeCompare(a.orderNumber));
            setOrders(ordersList.filter((o: Order) => o.orderNumber));
        } catch {
            setError('Failed to load orders');
        } finally {
            setOrdersLoading(false);
        }
    };

    const handleStatusUpdate = async (sk: string, status: string) => {
        try {
            await fetch(`${API}/srt/v1/orders/${sk}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status }),
            });
            setOrders(orders.map(o => o.sk === sk ? { ...o, status } : o));
        } catch {
            setError('Failed to update status');
        }
    };

    const handleLogout = async () => {
        await signOut();
        router.push('/admin');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const body = { name: form.name, description: form.description, price: parseFloat(form.price), category: form.category, imageUrl: form.imageUrl };
        try {
            if (editingDish) {
                await fetch(`${API}/srt/v1/dishes/${editingDish.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
            } else {
                await fetch(`${API}/srt/v1/dishes`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
            }
            setShowForm(false);
            setEditingDish(null);
            setIsNewCategory(false);
            setForm({ name: '', description: '', price: '', category: '', imageUrl: '' });
            loadDishes();
        } catch {
            setError('Failed to save dish');
        }
    };

    const handleEdit = (dish: Dish) => {
        setEditingDish(dish);
        setIsNewCategory(false);
        setForm({ name: dish.name, description: dish.description, price: dish.price.toString(), category: dish.category, imageUrl: dish.imageUrl || '' });
        setShowForm(true);
    };

    const handleDelete = async (id: string) => {
        if (!confirm('Delete this dish?')) return;
        try {
            await fetch(`${API}/srt/v1/dishes/${id}`, { method: 'DELETE' });
            loadDishes();
        } catch {
            setError('Failed to delete dish');
        }
    };

    const handleCancel = () => {
        setShowForm(false);
        setEditingDish(null);
        setIsNewCategory(false);
        setForm({ name: '', description: '', price: '', category: '', imageUrl: '' });
    };

    const inputStyle = {
        background: 'rgba(255,255,255,0.06)',
        border: '1px solid rgba(255,255,255,0.12)',
        borderRadius: '8px',
        padding: '10px 14px',
        color: '#fff',
        fontSize: '14px',
        width: '100%',
        outline: 'none',
        boxSizing: 'border-box' as const,
    };

    // Filtered orders
    const filteredOrders = orders.filter(o => {
        if (filterName && !o.customerName?.toLowerCase().includes(filterName.toLowerCase())) return false;
        if (filterCard && !o.cardLast4?.includes(filterCard)) return false;
        if (filterStatus && o.status !== filterStatus) return false;
        if (filterDate && o.orderDate !== filterDate) return false;
        if (filterDateFrom && o.orderDate < filterDateFrom) return false;
        if (filterDateTo && o.orderDate > filterDateTo) return false;
        return true;
    });

    // Stats
    const todayStr = new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD in local time
    const anyFilterActive = filterDate || filterDateFrom || filterDateTo || filterName || filterCard || filterStatus;
    const statsOrders = anyFilterActive ? filteredOrders : orders.filter(o => o.orderDate === todayStr);
    const statsRevenue = statsOrders.reduce((sum, o) => sum + (o.grandTotal || 0), 0);
    const preparingCount = statsOrders.filter(o => o.status === 'PREPARING').length;
    const readyCount = statsOrders.filter(o => o.status === 'READY').length;

    const statsLabel = filterDate
        ? filterDate
        : (filterDateFrom && filterDateTo)
            ? `${filterDateFrom} → ${filterDateTo}`
            : filterDateFrom
                ? `From ${filterDateFrom}`
                : 'Today';

    const OrderModal = ({ order, onClose }: { order: Order; onClose: () => void }) => {
    const handlePrint = () => {
        const printContent = document.getElementById('print-receipt');
        if (!printContent) return;
        const win = window.open('', '_blank');
        if (!win) return;
        win.document.write(`
            <html>
            <head>
                <title>Receipt ${order.orderNumber}</title>
                <style>
                    body { font-family: 'Courier New', monospace; width: 300px; margin: 0 auto; padding: 20px; }
                    .header { background: #1A1A1A; color: white; padding: 16px; text-align: center; }
                    .logo { color: #C0392B; font-size: 20px; font-weight: 800; letter-spacing: 2px; }
                    .sub { color: rgba(255,255,255,0.5); font-size: 10px; margin-top: 4px; }
                    .body { padding: 16px; }
                    .row { display: flex; justify-content: space-between; font-size: 12px; color: #333; padding: 3px 0; }
                    .item { display: flex; justify-content: space-between; font-size: 12px; padding: 5px 0; border-bottom: 1px dotted #eee; }
                    .divider { border: none; border-top: 1px dashed #ccc; margin: 10px 0; }
                    .total { display: flex; justify-content: space-between; font-size: 14px; font-weight: 700; padding: 8px 0 0; }
                    .footer { text-align: center; padding: 12px; font-size: 10px; color: #999; letter-spacing: 0.5px; }
                    .section-title { font-size: 10px; color: #999; margin-bottom: 6px; letter-spacing: 0.5px; }
                    .barcode { display: flex; justify-content: center; gap: 2px; margin: 10px 0 4px; }
                    .bar { background: #1A1A1A; height: 28px; }
                </style>
            </head>
            <body>${printContent.innerHTML}</body>
            </html>
        `);
        win.document.close();
        win.print();
        win.close();
    };

    const bars = [2,3,1,4,2,1,3,2,4,1,3,2,1,4,2,3,1,2,4,1,3,2,1,4,2,1,3];

    return (
        <div
            onClick={onClose}
            style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
            <div onClick={e => e.stopPropagation()} style={{ maxHeight: '90vh', overflowY: 'auto' }}>

                {/* Close button */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '8px' }}>
                    <button onClick={onClose}
                        style={{ background: 'rgba(255,255,255,0.15)', border: 'none', color: '#fff', borderRadius: '8px', padding: '6px 14px', cursor: 'pointer', fontSize: '13px', fontWeight: 600 }}>
                        ✕ Close
                    </button>
                </div>

                {/* Thermal Receipt */}
                <div id="print-receipt" style={{ background: '#fff', width: '320px', fontFamily: "'Courier New', monospace", borderRadius: '4px', overflow: 'hidden' }}>

                    {/* Header */}
                    <div style={{ background: '#1A1A1A', padding: '20px', textAlign: 'center' }}>
                        <div style={{ color: '#C0392B', fontSize: '20px', fontWeight: 800, letterSpacing: '2px' }}>SPICE ROAD</div>
                        <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: '10px', marginTop: '4px', letterSpacing: '1px' }}>ATLANTA, GA · INDO-AMERICAN FUSION</div>
                    </div>

                    {/* Body */}
                    <div style={{ padding: '20px' }}>

                        {/* Order info */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Order #</span><span style={{ fontWeight: 700 }}>{order.orderNumber}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Date</span><span>{order.orderDate}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Time</span><span>{order.orderTime}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Customer</span><span>{order.customerName}</span>
                        </div>

                        <hr style={{ border: 'none', borderTop: '1px dashed #ccc', margin: '12px 0' }} />

                        {/* Items */}
                        <div style={{ fontSize: '10px', color: '#999', marginBottom: '8px', letterSpacing: '0.5px' }}>ITEMS ORDERED</div>
                        {order.items?.map((item, i) => (
                            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', padding: '5px 0', borderBottom: '1px dotted #eee', color: '#333' }}>
                                <span>{item.name} x{item.quantity}</span>
                                <span>${(item.price * item.quantity).toFixed(2)}</span>
                            </div>
                        ))}

                        <hr style={{ border: 'none', borderTop: '1px dashed #ccc', margin: '12px 0' }} />

                        {/* Totals */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#555', padding: '3px 0' }}>
                            <span>Subtotal</span><span>${order.subtotal?.toFixed(2)}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#555', padding: '3px 0' }}>
                            <span>Tax (8%)</span><span>${order.tax?.toFixed(2)}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '16px', fontWeight: 700, padding: '10px 0 0', color: '#1A1A1A' }}>
                            <span>TOTAL</span><span>${order.grandTotal?.toFixed(2)}</span>
                        </div>

                        <hr style={{ border: 'none', borderTop: '1px dashed #ccc', margin: '12px 0' }} />

                        {/* Payment */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Card</span><span>VISA ···· {order.cardLast4}</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Status</span>
                            <span style={{ color: '#2ecc71', fontWeight: 700 }}>APPROVED</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#333', padding: '3px 0' }}>
                            <span>Order Status</span>
                            <span style={{ fontWeight: 700 }}>{order.status}</span>
                        </div>

                        {/* Barcode */}
                        <div style={{ display: 'flex', justifyContent: 'center', gap: '2px', margin: '16px 0 4px' }}>
                            {bars.map((w, i) => (
                                <div key={i} style={{ background: '#1A1A1A', height: '32px', width: `${w}px` }} />
                            ))}
                        </div>
                        <div style={{ fontSize: '9px', color: '#ccc', textAlign: 'center' }}>
                            {order.orderNumber}-{order.orderDate?.replace(/-/g, '')}-{order.cardLast4}
                        </div>
                    </div>

                    {/* Footer */}
                    <div style={{ textAlign: 'center', padding: '12px 20px 20px', fontSize: '10px', color: '#999', letterSpacing: '0.5px', borderTop: '1px dashed #eee' }}>
                        THANK YOU FOR YOUR ORDER!<br />
                        spiceroadtruck.com
                    </div>
                </div>

                {/* Print button below receipt */}
                <button onClick={handlePrint}
                    style={{ width: '320px', marginTop: '10px', background: '#1A1A1A', color: '#fff', border: 'none', borderRadius: '8px', padding: '12px', fontSize: '13px', fontWeight: 700, cursor: 'pointer', letterSpacing: '0.5px' }}>
                    🖨 Print Receipt
                </button>
            </div>
        </div>
    );
};            

    return (
        <div style={{ minHeight: '100vh', background: '#111', color: '#fff', padding: '2rem' }}>

            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <img src="/images/logo/srt-logo-dark.svg" width={40} height={40} alt="SRT" />
                    <div>
                        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600 }}>Admin Dashboard</h1>
                        <p style={{ margin: 0, fontSize: '13px', color: 'rgba(255,255,255,0.4)' }}>Spice Road Truck</p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                    {activeTab === 'dishes' && (
                        <button onClick={() => { setEditingDish(null); setIsNewCategory(false); setForm({ name: '', description: '', price: '', category: '', imageUrl: '' }); setShowForm(true); }}
                            style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 20px', cursor: 'pointer', fontWeight: 600 }}>
                            + Add Dish
                        </button>
                    )}
                    {activeTab === 'orders' && (
                        <button onClick={loadOrders}
                            style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 20px', cursor: 'pointer' }}>
                            ↻ Refresh
                        </button>
                    )}
                    <button onClick={handleLogout}
                        style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 20px', cursor: 'pointer' }}>
                        Logout
                    </button>
                </div>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', borderBottom: '1px solid rgba(255,255,255,0.08)', marginBottom: '1.5rem' }}>
                {(['dishes', 'orders'] as const).map(tab => (
                    <button key={tab} onClick={() => setActiveTab(tab)}
                        style={{ padding: '12px 24px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: 600, color: activeTab === tab ? '#fff' : 'rgba(255,255,255,0.4)', borderBottom: activeTab === tab ? '2px solid #C0392B' : '2px solid transparent', marginBottom: '-1px' }}>
                        {tab === 'dishes' ? '🍽️ Dishes' : '📦 Orders'}
                        {tab === 'orders' && orders.filter(o => o.status === 'NEW').length > 0 && (
                            <span style={{ marginLeft: '6px', background: '#C0392B', color: '#fff', borderRadius: '10px', padding: '1px 7px', fontSize: '11px' }}>
                                {orders.filter(o => o.status === 'NEW').length}
                            </span>
                        )}
                    </button>
                ))}
            </div>

            {error && <p style={{ color: '#E74C3C', marginBottom: '1rem' }}>{error}</p>}

            {/* DISHES TAB */}
            {activeTab === 'dishes' && (
                <>
                    {showForm && (
                        <div style={{ background: '#1E1E1E', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '1.5rem', marginBottom: '2rem' }}>
                            <h2 style={{ margin: '0 0 1.5rem', fontSize: '16px' }}>{editingDish ? 'Edit Dish' : 'Add New Dish'}</h2>
                            <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <input style={inputStyle} placeholder="Dish name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
                                <input style={inputStyle} placeholder="Price (e.g. 7.99)" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })} required type="number" step="0.01" />
                                <select style={inputStyle} value={isNewCategory ? '__new__' : form.category}
                                    onChange={e => {
                                        if (e.target.value === '__new__') { setIsNewCategory(true); setForm({ ...form, category: '' }); }
                                        else { setIsNewCategory(false); setForm({ ...form, category: e.target.value }); }
                                    }}>
                                    <option value="">Select category</option>
                                    {uniqueCategories.map(c => <option key={c} value={c}>{c}</option>)}
                                    <option value="__new__">+ Add new category...</option>
                                </select>
                                {isNewCategory ? (
                                    <input style={inputStyle} placeholder="Type new category name" value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} required autoFocus />
                                ) : (
                                    <input style={inputStyle} placeholder="Image URL (optional)" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} />
                                )}
                                {isNewCategory && (
                                    <input style={{ ...inputStyle, gridColumn: '1 / -1' }} placeholder="Image URL (optional)" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} />
                                )}
                                <textarea style={{ ...inputStyle, gridColumn: '1 / -1', minHeight: '80px', resize: 'vertical' }} placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} required />
                                <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '12px' }}>
                                    <button type="submit" style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer', fontWeight: 600 }}>
                                        {editingDish ? 'Update Dish' : 'Add Dish'}
                                    </button>
                                    <button type="button" onClick={handleCancel} style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer' }}>
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        </div>
                    )}

                    {loading ? (
                        <p style={{ color: 'rgba(255,255,255,0.4)' }}>Loading dishes...</p>
                    ) : (
                        <div style={{ background: '#1E1E1E', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', overflow: 'hidden' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
                                        <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>ID</th>
                                        <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>NAME</th>
                                        <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>CATEGORY</th>
                                        <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>PRICE</th>
                                        <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>ACTIONS</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dishes.map((dish, i) => (
                                        <tr key={dish.id} style={{ borderBottom: i < dishes.length - 1 ? '1px solid rgba(255,255,255,0.05)' : 'none' }}>
                                            <td style={{ padding: '14px 16px', fontSize: '13px', color: 'rgba(255,255,255,0.4)' }}>{dish.id}</td>
                                            <td style={{ padding: '14px 16px', fontSize: '14px' }}>{dish.name}</td>
                                            <td style={{ padding: '14px 16px' }}>
                                                <span style={{ background: 'rgba(192,57,43,0.2)', color: '#E67E22', padding: '3px 10px', borderRadius: '20px', fontSize: '12px' }}>
                                                    {dish.category}
                                                </span>
                                            </td>
                                            <td style={{ padding: '14px 16px', fontSize: '14px', fontWeight: 500 }}>${dish.price.toFixed(2)}</td>
                                            <td style={{ padding: '14px 16px', display: 'flex', gap: '8px' }}>
                                                <button onClick={() => handleEdit(dish)} style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '6px', padding: '6px 14px', cursor: 'pointer', fontSize: '13px' }}>Edit</button>
                                                <button onClick={() => handleDelete(dish.id)} style={{ background: 'rgba(192,57,43,0.15)', color: '#E74C3C', border: '1px solid rgba(192,57,43,0.3)', borderRadius: '6px', padding: '6px 14px', cursor: 'pointer', fontSize: '13px' }}>Delete</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}

            {/* ORDERS TAB */}
            {activeTab === 'orders' && (
                <>
                    {/* Stats */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px', marginBottom: '1.5rem' }}>
                        {[
                            { label: `${statsLabel} Orders`, value: statsOrders.length, color: '#C0392B' },
                            { label: 'Preparing', value: preparingCount, color: '#facc15' },
                            { label: 'Ready', value: readyCount, color: '#4ade80' },
                            { label: `${statsLabel} Revenue`, value: `$${statsRevenue.toFixed(2)}`, color: '#C0392B' },
                        ].map((stat, i) => (
                            <div key={i} style={{ background: '#1E1E1E', borderRadius: '10px', padding: '16px', border: '1px solid rgba(255,255,255,0.06)' }}>
                                <div style={{ fontSize: '22px', fontWeight: 700, color: stat.color }}>{stat.value}</div>
                                <div style={{ fontSize: '12px', color: 'rgba(255,255,255,0.4)', marginTop: '4px' }}>{stat.label}</div>
                            </div>
                        ))}
                    </div>

                    {/* Filters */}
                    <div style={{ display: 'flex', gap: '10px', marginBottom: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
                        <input
                            style={{ ...inputStyle, width: '180px' }}
                            placeholder="Search by name..."
                            value={filterName}
                            onChange={e => setFilterName(e.target.value)}
                        />
                        <input
                            style={{ ...inputStyle, width: '130px' }}
                            placeholder="Card last 4..."
                            value={filterCard}
                            onChange={e => setFilterCard(e.target.value)}
                            maxLength={4}
                        />
                        <select
                            style={{ ...inputStyle, width: '140px' }}
                            value={filterStatus}
                            onChange={e => setFilterStatus(e.target.value)}>
                            <option value="">All Status</option>
                            <option value="NEW">New</option>
                            <option value="PREPARING">Preparing</option>
                            <option value="READY">Ready</option>
                            <option value="COMPLETED">Completed</option>
                        </select>

                        <span style={{ color: 'rgba(255,255,255,0.2)', fontSize: '13px' }}>|</span>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.4)' }}>Date</span>
                            <input
                                style={{ ...inputStyle, width: '150px' }}
                                type="date"
                                value={filterDate}
                                onChange={e => { setFilterDate(e.target.value); setFilterDateFrom(''); setFilterDateTo(''); }}
                            />
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.4)' }}>Range</span>
                            <input
                                style={{ ...inputStyle, width: '150px' }}
                                type="date"
                                value={filterDateFrom}
                                onChange={e => { setFilterDateFrom(e.target.value); setFilterDate(''); }}
                            />
                            <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.4)' }}>to</span>
                            <input
                                style={{ ...inputStyle, width: '150px' }}
                                type="date"
                                value={filterDateTo}
                                onChange={e => { setFilterDateTo(e.target.value); setFilterDate(''); }}
                            />
                        </div>

                        {(filterName || filterCard || filterDate || filterStatus || filterDateFrom || filterDateTo) && (
                            <button
                                onClick={() => { setFilterName(''); setFilterCard(''); setFilterDate(''); setFilterStatus(''); setFilterDateFrom(''); setFilterDateTo(''); }}
                                style={{ background: 'rgba(255,255,255,0.06)', color: '#fff', border: '1px solid rgba(255,255,255,0.12)', borderRadius: '8px', padding: '10px 16px', cursor: 'pointer', fontSize: '13px' }}>
                                Clear Filters
                            </button>
                        )}
                    </div>

                    {ordersLoading ? (
                        <p style={{ color: 'rgba(255,255,255,0.4)' }}>Loading orders...</p>
                    ) : filteredOrders.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '3rem', color: 'rgba(255,255,255,0.3)' }}>
                            <div style={{ fontSize: '48px', marginBottom: '12px' }}>📦</div>
                            <p>No orders found</p>
                        </div>
                    ) : (
                        <div style={{ background: '#1E1E1E', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', overflow: 'hidden' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
                                        {['ORDER #', 'NAME', 'ITEMS', 'TOTAL', 'CARD', 'DATE', 'STATUS', 'ACTIONS'].map(h => (
                                            <th key={h} style={{ padding: '12px 14px', textAlign: 'left', fontSize: '11px', color: 'rgba(255,255,255,0.4)', fontWeight: 600, letterSpacing: '0.5px' }}>{h}</th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredOrders.map((order, i) => {
                                        const statusStyle = STATUS_COLORS[order.status] || STATUS_COLORS.NEW;
                                        return (
                                            <tr key={order.sk} style={{ borderBottom: i < filteredOrders.length - 1 ? '1px solid rgba(255,255,255,0.04)' : 'none' }}>
                                                <td style={{ padding: '12px 14px', fontSize: '13px', color: '#C0392B', fontWeight: 700 }}>{order.orderNumber}</td>
                                                <td style={{ padding: '12px 14px', fontSize: '13px' }}>
                                                    <div>{order.customerName}</div>
                                                    <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)' }}>{order.customerEmail}</div>
                                                </td>
                                                <td style={{ padding: '12px 14px', fontSize: '13px', color: 'rgba(255,255,255,0.6)' }}>
                                                    {order.items?.length || 0} item{(order.items?.length || 0) !== 1 ? 's' : ''}
                                                </td>
                                                <td style={{ padding: '12px 14px', fontSize: '13px', fontWeight: 700 }}>${(order.grandTotal || 0).toFixed(2)}</td>
                                                <td style={{ padding: '12px 14px', fontSize: '13px', color: 'rgba(255,255,255,0.5)' }}>••• {order.cardLast4}</td>
                                                <td style={{ padding: '12px 14px', fontSize: '12px', color: 'rgba(255,255,255,0.5)' }}>
                                                    <div>{order.orderDate}</div>
                                                    <div>{order.orderTime}</div>
                                                </td>
                                                <td style={{ padding: '12px 14px' }}>
                                                    <span style={{ background: statusStyle.bg, color: statusStyle.color, padding: '3px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: 700 }}>
                                                        {order.status}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '12px 14px', display: 'flex', gap: '8px', alignItems: 'center' }}>
                                                    <button
                                                        onClick={() => setViewingOrder(order)}
                                                        style={{ background: 'rgba(255,255,255,0.06)', color: '#fff', border: '1px solid rgba(255,255,255,0.12)', borderRadius: '6px', padding: '5px 10px', cursor: 'pointer', fontSize: '12px', whiteSpace: 'nowrap' }}>
                                                        View Bill
                                                    </button>
                                                    <select value={order.status} onChange={e => handleStatusUpdate(order.sk, e.target.value)}
                                                        style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', borderRadius: '6px', padding: '5px 8px', color: '#fff', fontSize: '12px', cursor: 'pointer', outline: 'none' }}>
                                                        <option value="NEW">New</option>
                                                        <option value="PREPARING">Preparing</option>
                                                        <option value="READY">Ready</option>
                                                        <option value="COMPLETED">Completed</option>
                                                    </select>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    )}
                </>
            )}

            {viewingOrder && <OrderModal order={viewingOrder} onClose={() => setViewingOrder(null)} />}
        </div>
    );
}