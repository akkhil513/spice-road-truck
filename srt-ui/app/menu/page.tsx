'use client';
import { useEffect, useState } from 'react';
import { getAllDishes, Dish } from '@/lib/api';

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

export default function MenuPage() {
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');

  useEffect(() => {
    getAllDishes()
      .then(setDishes)
      .catch(() => setError('Failed to load menu. Please try again.'))
      .finally(() => setLoading(false));
  }, []);

  // Dynamically get unique categories from actual dish data
  const uniqueCategories = Array.from(
    new Set(dishes.map(d => d.category).filter(Boolean))
  ) as string[];

  const categories = ['All', ...uniqueCategories];

  // Filter for selected category tab
  const filtered = activeCategory === 'All'
    ? dishes
    : dishes.filter(d => d.category === activeCategory);

  // Group by category dynamically
  const grouped = uniqueCategories.reduce((acc, cat) => {
    const items = dishes.filter(d => d.category === cat);
    if (items.length > 0) acc[cat] = items;
    return acc;
  }, {} as Record<string, Dish[]>);

  // Dishes with no category
  const uncategorized = dishes.filter(d => !d.category);
  if (uncategorized.length > 0) grouped['Uncategorized'] = uncategorized;

  return (
    <div className="min-h-screen bg-[#FFF8F0]">

      {/* ── HERO ── */}
      <section className="bg-[#1A1A1A] py-20 text-center relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{
            backgroundImage: `radial-gradient(circle at 30% 50%, #C0392B 0%, transparent 50%),
                              radial-gradient(circle at 70% 50%, #E67E22 0%, transparent 50%)`,
          }}
        />
        <div className="relative">
          <span className="text-[#E67E22] font-semibold text-sm uppercase tracking-widest">
            What We Serve
          </span>
          <h1 className="text-5xl font-black text-white mt-2 mb-4">Our Menu</h1>
          <p className="text-gray-400 max-w-xl mx-auto">
            Bold Indo-American street food. Fresh ingredients, live tandoor, authentic spices.
          </p>
        </div>
      </section>

      {/* ── CATEGORY FILTER ── */}
      <div className="sticky top-16 z-40 bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-3 flex gap-2 overflow-x-auto">
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              className={`whitespace-nowrap px-4 py-2 rounded-full font-semibold text-sm transition-all duration-200 flex-shrink-0 ${
                activeCategory === cat
                  ? 'bg-[#C0392B] text-white shadow-md'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {cat !== 'All' ? `${getCategoryEmoji(cat)} ${cat}` : cat}
            </button>
          ))}
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-12">

        {/* Loading */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-24 gap-4">
            <div className="w-12 h-12 border-4 border-[#C0392B] border-t-transparent rounded-full animate-spin"/>
            <p className="text-gray-500 font-medium">Loading menu...</p>
          </div>
        )}

        {/* Error */}
        {error && (
          <div className="text-center py-24">
            <p className="text-red-500 font-semibold text-lg">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="mt-4 bg-[#C0392B] text-white px-6 py-2 rounded-full font-semibold"
            >
              Try Again
            </button>
          </div>
        )}

        {/* ALL view — grouped by category */}
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
                  <div className="flex-1 h-px bg-gray-200 ml-4"/>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {items.map(dish => (
                    <DishCard key={dish.id} dish={dish} getCategoryEmoji={getCategoryEmoji}/>
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
            {filtered.length === 0
              ? <EmptyState />
              : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filtered.map(dish => (
                    <DishCard key={dish.id} dish={dish} getCategoryEmoji={getCategoryEmoji}/>
                  ))}
                </div>
              )
            }
          </div>
        )}
      </div>
    </div>
  );
}

function DishCard({ dish, getCategoryEmoji }: { dish: Dish; getCategoryEmoji: (cat: string) => string }) {
  return (
    <div className="bg-white rounded-2xl overflow-hidden shadow-md hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-gray-100 flex flex-col">
      <div className="h-48 bg-gradient-to-br from-[#C0392B]/10 to-[#E67E22]/20 flex items-center justify-center relative overflow-hidden">
        {dish.imageUrl && dish.imageUrl.trim() !== '' ?(
          <img src={dish.imageUrl} alt={dish.name} className="w-full h-full object-cover"/>
        ) : (
          <span className="text-6xl">{getCategoryEmoji(dish.category || '')}</span>
        )}
        {dish.category && (
          <span className="absolute top-3 left-3 bg-[#C0392B] text-white text-xs font-semibold px-2 py-1 rounded-full">
            {dish.category}
          </span>
        )}
      </div>
      <div className="p-5 flex flex-col flex-1">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="font-black text-[#1A1A1A] text-lg leading-tight">{dish.name}</h3>
          <span className="text-[#C0392B] font-black text-lg whitespace-nowrap">
            ${dish.price.toFixed(2)}
          </span>
        </div>
        {dish.description && (
          <p className="text-gray-500 text-sm leading-relaxed flex-1 line-clamp-3">
            {dish.description}
          </p>
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