'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MapPin, Clock, ChevronRight, Star, Utensils, Users, Award, Pencil, Check, X } from 'lucide-react';
import { useAuth } from '@/lib/authContext';

const defaultSchedule = [
  { day: 'Thursday', time: '11:00 AM - 7:00 PM', location: 'North Georgia Premium Outlet', address: 'Near LOFT Outlet' },
  { day: 'Friday', time: '11:00 AM - 7:00 PM', location: 'North Georgia Premium Outlet', address: 'Near LOFT Outlet' },
  { day: 'Saturday', time: '10:00 AM - 9:00 PM', location: 'North Georgia Premium Outlet', address: 'Near LOFT Outlet' },
  { day: 'Sunday', time: '1:00 PM - 6:00 PM', location: 'North Georgia Premium Outlet', address: 'Near LOFT Outlet' },
];

const services = [
  { title: 'Birthday Party', description: 'Make your birthday unforgettable with our live food truck experience.', emoji: '🎂' },
  { title: 'Corporate Events', description: 'Impress your team and clients with bold Indo-American flavors.', emoji: '🏢' },
  { title: 'Graduation Party', description: 'Celebrate your milestone with a feast your guests will talk about.', emoji: '🎓' },
  { title: 'Wedding Catering', description: 'Authentic live tandoor grill catering for your special day.', emoji: '💍' },
  { title: 'Pooja & Housewarming', description: 'Pure vegetarian live catering for religious and cultural events.', emoji: '🪔' },
  { title: 'Private Events', description: 'Any occasion deserves great food. We bring the truck to you.', emoji: '🎉' },
];

const stats = [
  { icon: <Utensils size={28} />, value: '50+', label: 'Menu Items' },
  { icon: <Users size={28} />, value: '10K+', label: 'Happy Customers' },
  { icon: <Star size={28} />, value: '4.9', label: 'Average Rating' },
  { icon: <Award size={28} />, value: '5+', label: 'Years Serving Atlanta' },
];

interface ScheduleItem {
  day: string;
  time: string;
  location: string;
  address: string;
}

function getWeekDates(schedule: ScheduleItem[]) {
  const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const today = new Date();
  const todayDay = today.getDay(); // 0 = Sunday

  return schedule.map(item => {
    const itemDayIndex = days.indexOf(item.day);
    if (itemDayIndex === -1) return { ...item, date: null, isPast: false, isToday: false };

    const diff = itemDayIndex - todayDay;
    const date = new Date(today);
    date.setDate(today.getDate() + diff);

    const isPast = diff < 0;
    const isToday = diff === 0;

    return {
      ...item,
      date,
      isPast,
      isToday,
    };
  });
}

export default function Home() {
  const { isAdmin } = useAuth();
  const [, setScrollY] = useState(0);
  const [schedule, setSchedule] = useState<ScheduleItem[]>(defaultSchedule);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<ScheduleItem | null>(null);

  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', handleScroll);
    // Load saved schedule from localStorage
    const saved = localStorage.getItem('srt-schedule');
    if (saved) setSchedule(JSON.parse(saved));
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleEditStart = (index: number) => {
    setEditingIndex(index);
    setEditForm({ ...schedule[index] });
  };

  const handleEditSave = (index: number) => {
    if (!editForm) return;
    const updated = schedule.map((item, i) => i === index ? editForm : item);
    setSchedule(updated);
    localStorage.setItem('srt-schedule', JSON.stringify(updated));
    setEditingIndex(null);
    setEditForm(null);
  };

  const handleEditCancel = () => {
    setEditingIndex(null);
    setEditForm(null);
  };

  const inputStyle: React.CSSProperties = {
    background: 'rgba(0,0,0,0.05)',
    border: '1px solid #e5e7eb',
    borderRadius: '6px',
    padding: '6px 10px',
    fontSize: '13px',
    width: '100%',
    outline: 'none',
    color: '#1A1A1A',
  };

  return (
    <div className="min-h-screen">

      {/* HERO */}
      <section className="relative min-h-screen bg-[#1A1A1A] flex items-center overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: `radial-gradient(circle at 20% 50%, #C0392B 0%, transparent 50%), radial-gradient(circle at 80% 20%, #E67E22 0%, transparent 40%)` }}
        />
        <div className="relative max-w-7xl mx-auto px-4 py-20 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <div className="inline-flex items-center gap-2 bg-[#C0392B]/20 border border-[#C0392B]/40 rounded-full px-4 py-1.5 mb-6">
              <span className="text-[#E67E22] text-sm font-semibold">🌶️ Now Serving Atlanta</span>
            </div>
            <h1 className="text-5xl lg:text-7xl font-black text-white leading-tight mb-4">
              Spice Road
              <span className="block text-[#C0392B]">Truck</span>
            </h1>
            <p className="text-xl text-gray-300 mb-4 font-medium">Discover Indo-American Street Fusion</p>
            <p className="text-gray-400 mb-8 leading-relaxed max-w-md">
              The best desi food truck with bold flavors under one roof. Live tandoor grill, Frankies, wings, biryani and so much more.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link href="/menu" className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-8 py-3 rounded-full font-bold text-lg transition-all duration-300 flex items-center gap-2 shadow-lg hover:shadow-xl">
                Explore Menu <ChevronRight size={20} />
              </Link>
              <Link href="/order" className="border-2 border-white/30 hover:border-[#E67E22] text-white hover:text-[#E67E22] px-8 py-3 rounded-full font-bold text-lg transition-all duration-300">
                Order Online
              </Link>
            </div>
          </div>
          <div className="hidden lg:flex justify-center items-center">
            <img src="/images/hero/truck.png" alt="Spice Road Truck" className="w-full max-w-lg object-contain"
              style={{ filter: 'drop-shadow(0 20px 40px rgba(192,57,43,0.6))' }} />
          </div>
          <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
            <div className="w-6 h-10 border-2 border-white/30 rounded-full flex items-start justify-center p-1">
              <div className="w-1 h-3 bg-white/50 rounded-full" />
            </div>
          </div>
        </div>
      </section>

      {/* STATS */}
      <section className="bg-[#C0392B] py-12">
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, i) => (
            <div key={i} className="flex flex-col items-center text-white text-center">
              <div className="mb-2 opacity-80">{stat.icon}</div>
              <div className="text-3xl font-black">{stat.value}</div>
              <div className="text-sm opacity-80 font-medium">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* TRUCK SCHEDULE */}
<section className="py-20 bg-[#FFF8F0]">
  <div className="max-w-7xl mx-auto px-4">
    <div className="text-center mb-12">
      <span className="text-[#C0392B] font-semibold text-sm uppercase tracking-widest">Find Us</span>
      <h2 className="text-4xl font-black text-[#1A1A1A] mt-2">Weekly Schedule</h2>
      <p className="text-gray-500 mt-2">Come find us at these locations every week</p>
      {isAdmin && (
        <p className="text-[#C0392B] text-xs mt-1 font-medium">
          ✏️ Admin mode — upcoming days are editable
        </p>
      )}
    </div>
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {getWeekDates(schedule).map((item, i) => {
        const canEdit = isAdmin && !item.isPast;
        const dateLabel = item.date
          ? item.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
          : '';

        return (
          <div key={i}
            className="bg-white rounded-2xl p-6 shadow-md hover:shadow-xl transition-shadow duration-300 border border-gray-100 relative"
            style={{ opacity: item.isPast ? 0.5 : 1 }}>

            {/* Admin edit button — only for today and upcoming */}
            {canEdit && editingIndex !== i && (
    <div style={{ position: 'absolute', top: '12px', right: '12px', display: 'flex', gap: '4px' }}>
        <button
            onClick={() => handleEditStart(i)}
            style={{ background: 'rgba(192,57,43,0.1)', border: '1px solid rgba(192,57,43,0.3)', borderRadius: '6px', padding: '4px 8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', color: '#C0392B', fontWeight: 600 }}>
            <Pencil size={12} /> Edit
        </button>
        <button
            onClick={() => {
                if (!confirm(`Remove ${item.day} from schedule?`)) return;
                const newSchedule = schedule.filter((_, idx) => idx !== i);
                setSchedule(newSchedule);
                localStorage.setItem('srt-schedule', JSON.stringify(newSchedule));
            }}
            style={{ background: 'rgba(192,57,43,0.1)', border: '1px solid rgba(192,57,43,0.3)', borderRadius: '6px', padding: '4px 8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', color: '#C0392B', fontWeight: 600 }}>
            <X size={12} />
        </button>
    </div>
)}

            {editingIndex === i && editForm ? (
              /* EDIT MODE */
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div>
                  <label style={{ fontSize: '11px', color: '#666', fontWeight: 600 }}>DAY</label>
                  <input style={inputStyle} value={editForm.day} onChange={e => setEditForm({ ...editForm, day: e.target.value })} />
                </div>
                <div>
                  <label style={{ fontSize: '11px', color: '#666', fontWeight: 600 }}>TIME</label>
                  <input style={inputStyle} value={editForm.time} onChange={e => setEditForm({ ...editForm, time: e.target.value })} placeholder="e.g. 11:00 AM - 7:00 PM" />
                </div>
                <div>
                  <label style={{ fontSize: '11px', color: '#666', fontWeight: 600 }}>LOCATION</label>
                  <input style={inputStyle} value={editForm.location} onChange={e => setEditForm({ ...editForm, location: e.target.value })} />
                </div>
                <div>
                  <label style={{ fontSize: '11px', color: '#666', fontWeight: 600 }}>ADDRESS</label>
                  <input style={inputStyle} value={editForm.address} onChange={e => setEditForm({ ...editForm, address: e.target.value })} />
                </div>
                <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
                  <button onClick={() => handleEditSave(i)}
                    style={{ flex: 1, background: '#C0392B', color: '#fff', border: 'none', borderRadius: '6px', padding: '8px', cursor: 'pointer', fontWeight: 600, fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px' }}>
                    <Check size={14} /> Save
                  </button>
                  <button onClick={handleEditCancel}
                    style={{ flex: 1, background: '#f3f4f6', color: '#666', border: 'none', borderRadius: '6px', padding: '8px', cursor: 'pointer', fontWeight: 600, fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px' }}>
                    <X size={14} /> Cancel
                  </button>
                </div>
              </div>
            ) : (
              /* VIEW MODE */
              <>
  <div className="w-12 h-12 bg-[#C0392B]/10 rounded-xl flex items-center justify-center mb-4">
    <span className="text-2xl">📍</span>
  </div>
  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '0' }}>
    <h3 className="font-black text-[#1A1A1A] text-xl" style={{ margin: 0 }}>{item.day}</h3>
    {item.isToday && (
      <span style={{ background: '#2ecc71', color: '#fff', fontSize: '10px', fontWeight: 700, padding: '3px 8px', borderRadius: '20px' }}>
        TODAY
      </span>
    )}
    {item.isPast && (
      <span style={{ background: '#ccc', color: '#fff', fontSize: '10px', fontWeight: 700, padding: '3px 8px', borderRadius: '20px' }}>
        PAST
      </span>
    )}
  </div>
  <p style={{ fontSize: '12px', color: '#9ca3af', fontWeight: 500, marginBottom: '4px', marginTop: '2px' }}>{dateLabel}</p>
  <div className="flex items-center gap-1 text-[#C0392B] text-sm font-semibold mb-3">
    <Clock size={14} />
    <span>{item.time}</span>
  </div>
  <p className="text-gray-700 font-medium text-sm">{item.location}</p>
  <p className="text-gray-400 text-xs mt-1 flex items-center gap-1">
    <MapPin size={12} />{item.address}
  </p>
</>
            )}
          </div>
        );
      })}
    </div>

    {/* Admin — Add/Remove buttons */}
    {isAdmin && (
      <div className="text-center mt-8">
        <button
          onClick={() => {
            const newSchedule = [...schedule, { day: 'New Day', time: '12:00 PM - 6:00 PM', location: 'Location', address: 'Address' }];
            setSchedule(newSchedule);
            localStorage.setItem('srt-schedule', JSON.stringify(newSchedule));
            setEditingIndex(newSchedule.length - 1);
            setEditForm(newSchedule[newSchedule.length - 1]);
          }}
          style={{ background: '#C0392B', color: '#fff', border: 'none', borderRadius: '20px', padding: '10px 24px', cursor: 'pointer', fontWeight: 600, fontSize: '14px' }}>
          + Add Day
        </button>
      </div>
    )}
  </div>
</section>

      {/* ABOUT US */}
      <section className="py-20 bg-[#1A1A1A]">
        <div className="max-w-7xl mx-auto px-4 grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div className="relative">
            <div className="bg-[#C0392B]/10 border border-[#C0392B]/20 rounded-3xl p-10 text-center">
              <div className="text-9xl mb-6">🍛</div>
              <blockquote className="text-gray-300 text-lg italic leading-relaxed">
                &quot;A culinary gem in Atlanta, known for its vibrant blend of Indian fusion and authentic classics.&quot;
              </blockquote>
            </div>
            <div className="absolute -top-4 -right-4 bg-[#E67E22] text-white text-xs font-bold px-3 py-1.5 rounded-full">Est. 2019</div>
          </div>
          <div>
            <span className="text-[#C0392B] font-semibold text-sm uppercase tracking-widest">Our Story</span>
            <h2 className="text-4xl font-black text-white mt-2 mb-6">About Us</h2>
            <p className="text-gray-300 leading-relaxed mb-4">
              Spice Road Truck is a culinary gem in Atlanta, GA, known for its vibrant blend of Indian fusion and authentic classics. We offer a unique dining experience with live tandoori oven grills and Frankies, allowing customers to customize their spice levels.
            </p>
            <p className="text-gray-300 leading-relaxed mb-4">
              Our menu features both vegetarian and non-vegetarian dishes inspired by American, Asian, and Indian cuisines — ensuring there is something for everyone.
            </p>
            <p className="text-gray-300 leading-relaxed mb-8">
              We also have a truck dedicated to pure vegetarian catering for events like puja, housewarmings, and corporate functions all over Atlanta.
            </p>
            <div className="flex gap-4">
              <Link href="/menu" className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-6 py-3 rounded-full font-semibold transition-colors">See Our Menu</Link>
              <Link href="/catering" className="border border-white/30 hover:border-[#E67E22] text-white hover:text-[#E67E22] px-6 py-3 rounded-full font-semibold transition-colors">Book Catering</Link>
            </div>
          </div>
        </div>
      </section>

      {/* SERVICES */}
      <section className="py-20 bg-[#FFF8F0]">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-[#C0392B] font-semibold text-sm uppercase tracking-widest">What We Offer</span>
            <h2 className="text-4xl font-black text-[#1A1A1A] mt-2">Catering Services</h2>
            <p className="text-gray-500 mt-2">We bring the flavors to your event</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {services.map((service, i) => (
              <div key={i} className="bg-white rounded-2xl p-6 shadow-md hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-gray-100 group">
                <div className="text-4xl mb-4">{service.emoji}</div>
                <h3 className="font-black text-[#1A1A1A] text-xl mb-2 group-hover:text-[#C0392B] transition-colors">{service.title}</h3>
                <p className="text-gray-500 text-sm leading-relaxed mb-4">{service.description}</p>
                <Link href="/catering" className="text-[#C0392B] font-semibold text-sm flex items-center gap-1 hover:gap-2 transition-all">
                  Request to Book <ChevronRight size={16} />
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 bg-[#C0392B]">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-4xl lg:text-5xl font-black text-white mb-4">Ready to Order?</h2>
          <p className="text-white/80 text-lg mb-8">Browse our full menu and place your order online. Pickup ready in 30 minutes.</p>
          <div className="flex flex-wrap gap-4 justify-center">
            <Link href="/order" className="bg-white text-[#C0392B] hover:bg-[#FFF8F0] px-8 py-3 rounded-full font-bold text-lg transition-colors shadow-lg">Order Now</Link>
            <Link href="/menu" className="border-2 border-white text-white hover:bg-white/10 px-8 py-3 rounded-full font-bold text-lg transition-colors">View Menu</Link>
          </div>
        </div>
      </section>

    </div>
  );
}