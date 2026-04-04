'use client';
import { useEffect, useState } from 'react';
import { getAllDishes, Dish } from '@/lib/api';
import { useAuth } from '@/lib/authContext';

const CATEGORY_EMOJIS: Record<string, string> = {
  'Appetizers': '🍢',
  'Street Style': '🛺',
  'Spicy Desi Bites': '🌶️',
  'Biryanis/Pulaos': '🍚',
  'Spicy Desi Meals': '🍛',
  'Indo-American Bites': '🌮',
  'Hot / Cold Drinks': '🥤',
  'Kids Junction': '🧒',
};

function getCategoryEmoji(cat: string) {
  return CATEGORY_EMOJIS[cat] || '🍽️';
}

const API = 'https://api.spiceroadtruck.com';
const CATEGORIES = ['Appetizers', 'Biryanis/Pulaos', 'Street Style', 'Spicy Desi Bites', 'Spicy Desi Meals', 'Indo-American Bites', 'Hot / Cold Drinks', 'Kids Junction'];

export default function MenuPage() {
  const { isAdmin } = useAuth();
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');
  const [showForm, setShowForm] = useState(false);
  const [editingDish, setEditingDish] = useState<Dish | null>(null);
  const [form, setForm] = useState({ name: '', description: '', price: '', category: '', imageUrl: '' });

  const loadDishes = () => {
    setLoading(true);
    getAllDishes()
      .then(setDishes)
      .catch(() => setError('Failed to load menu. Please try again.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadDishes(); }, []);

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
      setForm({ name: '', description: '', price: '', category: '', imageUrl: '' });
      loadDishes();
    } catch { setError('Failed to save dish'); }
  };

  const handleEdit = (dish: Dish) => {
    setEditingDish(dish);
    setForm({ name: dish.name, description: dish.description, price: dish.price.toString(), category: dish.category, imageUrl: dish.imageUrl || '' });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this dish?')) return;
    await fetch(`${API}/srt/v1/dishes/${id}`, { method: 'DELETE' });
    loadDishes();
  };

  const uniqueCategories = Array.from(new Set(dishes.map(d => d.category).filter(Boolean))) as string[];
  const categories = ['All', ...uniqueCategories];
  const filtered = activeCategory === 'All' ? dishes : dishes.filter(d => d.category === activeCategory);
  const grouped = uniqueCategories.reduce((acc, cat) => {
    const items = dishes.filter(d => d.category === cat);
    if (items.length > 0) acc[cat] = items;
    return acc;
  }, {} as Record<string, Dish[]>);

  const inputStyle: React.CSSProperties = {
    background: 'rgba(0,0,0,0.04)',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    padding: '10px 14px',
    color: '#1A1A1A',
    fontSize: '14px',
    width: '100%',
    outline: 'none',
    boxSizing: 'border-box',
  };

  return (
    <div className="min-h-screen bg-[#FFF8F0]">

      {/* HERO */}
      <section className="bg-[#1A1A1A] py-20 text-center relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: `radial-gradient(circle at 30% 50%, #C0392B 0%, transparent 50%), radial-gradient(circle at 70% 50%, #E67E22 0%, transparent 50%)` }}
        />
        <div className="relative">
          <span className="text-[#E67E22] font-semibold text-sm uppercase tracking-widest">What We Serve</span>
          <h1 className="text-5xl font-black text-white mt-2 mb-4">Our Menu</h1>
          <p className="text-gray-400 max-w-xl mx-auto">Bold Indo-American street food. Fresh ingredients, live tandoor, authentic spices.</p>
        </div>
      </section>

      {/* ADMIN FORM */}
      {isAdmin && showForm && (
        <div className="max-w-7xl mx-auto px-4 pt-8">
          <div style={{ background: '#1E1E1E', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '1.5rem' }}>
            <h2 style={{ color: '#fff', margin: '0 0 1.5rem', fontSize: '16px' }}>{editingDish ? 'Edit Dish' : 'Add New Dish'}</h2>
            <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <input style={{ ...inputStyle, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', color: '#fff' }} placeholder="Dish name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
              <input style={{ ...inputStyle, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', color: '#fff' }} placeholder="Price (e.g. 7.99)" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })} required type="number" step="0.01" />
              <select style={{ ...inputStyle, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', color: '#fff' }} value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} required>
                <option value="">Select category</option>
                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
              <input style={{ ...inputStyle, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', color: '#fff' }} placeholder="Image URL (optional)" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} />
              <textarea style={{ ...inputStyle, background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(255,255,255,0.12)', color: '#fff', gridColumn: '1 / -1', minHeight: '80px', resize: 'vertical' }} placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} required />
              <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '12px' }}>
                <button type="submit" style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer', fontWeight: 600 }}>
                  {editingDish ? 'Update Dish' : 'Add Dish'}
                </button>
                <button type="button" onClick={() => { setShowForm(false); setEditingDish(null); }} style={{ background: 'rgba(255,255,255,0.08)', color: '#fff', border: '1px solid rgba(255,255,255,0.15)', borderRadius: '8px', padding: '10px 24px', cursor: 'pointer' }}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CATEGORY FILTER + ADD BUTTON */}
      <div className="sticky top-16 z-40 bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-3 flex gap-2 overflow-x-auto items-center">
          {categories.map(cat => (
            <button key={cat} onClick={() => setActiveCategory(cat)}
              className={`whitespace-nowrap px-4 py-2 rounded-full font-semibold text-sm transition-all duration-200 flex-shrink-0 ${activeCategory === cat ? 'bg-[#C0392B] text-white shadow-md' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
              {cat !== 'All' ? `${getCategoryEmoji(cat)} ${cat}` : cat}
            </button>
          ))}
          {isAdmin && (
            <button onClick={() => { setEditingDish(null); setForm({ name: '', description: '', price: '', category: '', imageUrl: '' }); setShowForm(true); }}
              style={{ marginLeft: 'auto', background: '#C0392B', color: '#fff', border: 'none', borderRadius: '20px', padding: '8px 16px', cursor: 'pointer', fontWeight: 600, fontSize: '13px', flexShrink: 0 }}>
              + Add Dish
            </button>
          )}
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-12">

        {loading && (
          <div className="flex flex-col items-center justify-center py-24 gap-4">
            <div className="w-12 h-12 border-4 border-[#C0392B] border-t-transparent rounded-full animate-spin" />
            <p className="text-gray-500 font-medium">Loading menu...</p>
          </div>
        )}

        {error && (
          <div className="text-center py-24">
            <p className="text-red-500 font-semibold text-lg">{error}</p>
            <button onClick={() => window.location.reload()} className="mt-4 bg-[#C0392B] text-white px-6 py-2 rounded-full font-semibold">Try Again</button>
          </div>
        )}

        {/* ALL view */}
        {!loading && !error && activeCategory === 'All' && (
          <div className="space-y-16">
            {Object.entries(grouped).map(([category, items]) => (
              <section key={category}>
                <div className="flex items-center gap-3 mb-8">
                  <span className="text-3xl">{getCategoryEmoji(category)}</span>
                  <div>
                    <h2 className="text-2xl font-black text-[#1A1A1A]">{category}</h2>
                    <p className="text-sm text-gray-400">{items.length} item{items.length > 1 ? 's' : ''}</p>
                  </div>
                  <div className="flex-1 h-px bg-gray-200 ml-4" />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {items.map(dish => (
                    <DishCard key={dish.id} dish={dish} getCategoryEmoji={getCategoryEmoji} isAdmin={isAdmin} onEdit={handleEdit} onDelete={handleDelete} />
                  ))}
                </div>
              </section>
            ))}
            {Object.keys(grouped).length === 0 && <EmptyState />}
          </div>
        )}

        {/* FILTERED view */}
        {!loading && !error && activeCategory !== 'All' && (
          <div>
            <div className="flex items-center gap-3 mb-8">
              <span className="text-3xl">{getCategoryEmoji(activeCategory)}</span>
              <div>
                <h2 className="text-2xl font-black text-[#1A1A1A]">{activeCategory}</h2>
                <p className="text-sm text-gray-400">{filtered.length} item{filtered.length > 1 ? 's' : ''}</p>
              </div>
            </div>
            {filtered.length === 0 ? <EmptyState /> : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filtered.map(dish => (
                  <DishCard key={dish.id} dish={dish} getCategoryEmoji={getCategoryEmoji} isAdmin={isAdmin} onEdit={handleEdit} onDelete={handleDelete} />
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function DishCard({ dish, getCategoryEmoji, isAdmin, onEdit, onDelete }: {
  dish: Dish;
  getCategoryEmoji: (cat: string) => string;
  isAdmin: boolean;
  onEdit: (dish: Dish) => void;
  onDelete: (id: string) => void;
}) {
  return (
    <div className="bg-white rounded-2xl overflow-hidden shadow-md hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-gray-100 flex flex-col">
      <div className="h-48 bg-gradient-to-br from-[#C0392B]/10 to-[#E67E22]/20 flex items-center justify-center relative overflow-hidden">
        {dish.imageUrl && dish.imageUrl.trim() !== '' ? (
          <img src={dish.imageUrl} alt={dish.name} className="w-full h-full object-cover" />
        ) : (
          <span className="text-6xl">{getCategoryEmoji(dish.category || '')}</span>
        )}
        {dish.category && (
          <span className="absolute top-3 left-3 bg-[#C0392B] text-white text-xs font-semibold px-2 py-1 rounded-full">
            {dish.category}
          </span>
        )}
        {/* Admin buttons on card */}
        {isAdmin && (
          <div className="absolute top-3 right-3 flex gap-2">
            <button onClick={() => onEdit(dish)}
              style={{ background: 'rgba(0,0,0,0.7)', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 10px', cursor: 'pointer', fontSize: '12px', fontWeight: 600 }}>
              Edit
            </button>
            <button onClick={() => onDelete(dish.id!)}
              style={{ background: 'rgba(192,57,43,0.85)', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 10px', cursor: 'pointer', fontSize: '12px', fontWeight: 600 }}>
              Delete
            </button>
          </div>
        )}
      </div>
      <div className="p-5 flex flex-col flex-1">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="font-black text-[#1A1A1A] text-lg leading-tight">{dish.name}</h3>
          <span className="text-[#C0392B] font-black text-lg whitespace-nowrap">${dish.price.toFixed(2)}</span>
        </div>
        {dish.description && (
          <p className="text-gray-500 text-sm leading-relaxed flex-1 line-clamp-3">{dish.description}</p>
        )}
      </div>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="text-center py-24">
      <span className="text-6xl mb-4 block">🍽️</span>
      <h3 className="text-xl font-bold text-gray-600 mb-2">No dishes here yet</h3>
      <p className="text-gray-400">Check back soon — more items coming!</p>
    </div>
  );
}