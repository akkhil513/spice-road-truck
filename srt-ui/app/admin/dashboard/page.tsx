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

const API = 'https://api.spiceroadtruck.com';

export default function AdminDashboard() {
    const router = useRouter();
    const [dishes, setDishes] = useState<Dish[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editingDish, setEditingDish] = useState<Dish | null>(null);
    const [isNewCategory, setIsNewCategory] = useState(false);
    const [form, setForm] = useState({
        name: '', description: '', price: '', category: '', imageUrl: ''
    });

    const [uniqueCategories, setUniqueCategories] = useState<string[]>([]);

    useEffect(() => {
        checkAuthAndLoad();
    }, []);

    const checkAuthAndLoad = async () => {
        try {
            await fetchAuthSession();
            loadDishes();
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
        // Extract unique categories from loaded dishes
        const cats = [...new Set(data.map((d: Dish) => d.category).filter(Boolean))] as string[];
        setUniqueCategories(cats);
    } catch {
        setError('Failed to load dishes');
    } finally {
        setLoading(false);
    }
};

    const handleLogout = async () => {
        await signOut();
        router.push('/admin');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const body = {
            name: form.name,
            description: form.description,
            price: parseFloat(form.price),
            category: form.category,
            imageUrl: form.imageUrl,
        };

        try {
            if (editingDish) {
                await fetch(`${API}/srt/v1/dishes/${editingDish.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body),
                });
            } else {
                await fetch(`${API}/srt/v1/dishes`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body),
                });
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
        setForm({
            name: dish.name,
            description: dish.description,
            price: dish.price.toString(),
            category: dish.category,
            imageUrl: dish.imageUrl || '',
        });
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

    return (
        <div style={{ minHeight: '100vh', background: '#111', color: '#fff', padding: '2rem' }}>

            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <img src="/images/logo/srt-logo-dark.svg" width={40} height={40} alt="SRT" />
                    <div>
                        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600 }}>Admin Dashboard</h1>
                        <p style={{ margin: 0, fontSize: '13px', color: 'rgba(255,255,255,0.4)' }}>Spice Road Truck</p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                    <button
                        onClick={() => {
                            setEditingDish(null);
                            setIsNewCategory(false);
                            setForm({ name: '', description: '', price: '', category: '', imageUrl: '' });
                            setShowForm(true);
                        }}
                        style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 20px', cursor: 'pointer', fontWeight: 600 }}>
                        + Add Dish
                    </button>
                    <button
                        onClick={handleLogout}
                        style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 20px', cursor: 'pointer' }}>
                        Logout
                    </button>
                </div>
            </div>

            {error && <p style={{ color: '#E74C3C', marginBottom: '1rem' }}>{error}</p>}

            {/* Add/Edit Form */}
            {showForm && (
                <div style={{ background: '#1E1E1E', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '1.5rem', marginBottom: '2rem' }}>
                    <h2 style={{ margin: '0 0 1.5rem', fontSize: '16px' }}>{editingDish ? 'Edit Dish' : 'Add New Dish'}</h2>
                    <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>

                        <input
                            style={inputStyle}
                            placeholder="Dish name"
                            value={form.name}
                            onChange={e => setForm({ ...form, name: e.target.value })}
                            required
                        />

                        <input
                            style={inputStyle}
                            placeholder="Price (e.g. 7.99)"
                            value={form.price}
                            onChange={e => setForm({ ...form, price: e.target.value })}
                            required
                            type="number"
                            step="0.01"
                        />

                        {/* Category dropdown */}
                        <select
                            style={inputStyle}
                            value={isNewCategory ? '__new__' : form.category}
                            onChange={e => {
                                if (e.target.value === '__new__') {
                                    setIsNewCategory(true);
                                    setForm({ ...form, category: '' });
                                } else {
                                    setIsNewCategory(false);
                                    setForm({ ...form, category: e.target.value });
                                }
                            }}
                        >
                            <option value="">Select category</option>
                            {uniqueCategories.map(c => (
                                <option key={c} value={c}>{c}</option>
                            ))}
                            <option value="__new__">+ Add new category...</option>
                        </select>

                        {/* New category input — shows when + Add new is selected */}
                        {isNewCategory ? (
                            <input
                                style={inputStyle}
                                placeholder="Type new category name"
                                value={form.category}
                                onChange={e => setForm({ ...form, category: e.target.value })}
                                required
                                autoFocus
                            />
                        ) : (
                            <input
                                style={inputStyle}
                                placeholder="Image URL (optional)"
                                value={form.imageUrl}
                                onChange={e => setForm({ ...form, imageUrl: e.target.value })}
                            />
                        )}

                        {/* Show image URL below when new category is visible */}
                        {isNewCategory && (
                            <input
                                style={{ ...inputStyle, gridColumn: '1 / -1' }}
                                placeholder="Image URL (optional)"
                                value={form.imageUrl}
                                onChange={e => setForm({ ...form, imageUrl: e.target.value })}
                            />
                        )}

                        <textarea
                            style={{ ...inputStyle, gridColumn: '1 / -1', minHeight: '80px', resize: 'vertical' }}
                            placeholder="Description"
                            value={form.description}
                            onChange={e => setForm({ ...form, description: e.target.value })}
                            required
                        />

                        <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '12px' }}>
                            <button
                                type="submit"
                                style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer', fontWeight: 600 }}>
                                {editingDish ? 'Update Dish' : 'Add Dish'}
                            </button>
                            <button
                                type="button"
                                onClick={handleCancel}
                                style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer' }}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Dishes Table */}
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
                                        <button onClick={() => handleEdit(dish)} style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '6px', padding: '6px 14px', cursor: 'pointer', fontSize: '13px' }}>
                                            Edit
                                        </button>
                                        <button onClick={() => handleDelete(dish.id)} style={{ background: 'rgba(192,57,43,0.15)', color: '#E74C3C', border: '1px solid rgba(192,57,43,0.3)', borderRadius: '6px', padding: '6px 14px', cursor: 'pointer', fontSize: '13px' }}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}