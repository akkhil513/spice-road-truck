'use client';
import { useState } from 'react';
import { Phone, Mail, MapPin, Clock, Instagram, Facebook, Send, CheckCircle } from 'lucide-react';

export default function ContactPage() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', message: '' });
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    // Simulate form submission
    await new Promise(res => setTimeout(res, 1200));
    setSubmitted(true);
    setLoading(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  return (
    <div className="min-h-screen bg-[#FFF8F0]">

      {/* ── HERO ── */}
      <section className="bg-[#1A1A1A] py-20 text-center relative overflow-hidden">
        <div
          className="absolute inset-0 opacity-10"
          style={{
            backgroundImage: `radial-gradient(circle at 30% 50%, #C0392B 0%, transparent 50%),
                              radial-gradient(circle at 70% 50%, #E67E22 0%, transparent 50%)`,
          }}
        />
        <div className="relative">
          <span className="text-[#E67E22] font-semibold text-sm uppercase tracking-widest">
            Get In Touch
          </span>
          <h1 className="text-5xl font-black text-white mt-2 mb-4">Contact Us</h1>
          <p className="text-gray-400 max-w-xl mx-auto">
            Have a question? Want to book us for an event? We would love to hear from you.
          </p>
        </div>
      </section>

      <div className="max-w-7xl mx-auto px-4 py-16">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">

          {/* ── LEFT — Contact Info ── */}
          <div>

            {/* Truck photo placeholder */}
            <div className="rounded-2xl overflow-hidden mb-8 bg-[#1A1A1A] flex items-center justify-center h-56 relative">
              <img
                src="/images/hero/food-truck.jpg"
                alt="Spice Road Truck"
                className="w-full h-full object-cover opacity-80"
                style={{ mixBlendMode: 'multiply' }}
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#1A1A1A]/60 to-transparent" />
              <div className="absolute bottom-4 left-4">
                <p className="text-white font-black text-xl">Spice Road Truck</p>
                <p className="text-[#E67E22] text-sm">Hyderabad · Atlanta</p>
              </div>
            </div>

            {/* Address */}
            <div className="bg-white rounded-2xl p-6 shadow-md border border-gray-100 mb-4">
              <h3 className="font-black text-[#1A1A1A] text-lg mb-4 flex items-center gap-2">
                <div className="w-8 h-8 bg-[#C0392B]/10 rounded-lg flex items-center justify-center">
                  <MapPin size={16} className="text-[#C0392B]" />
                </div>
                Address
              </h3>
              <p className="text-gray-600 text-sm leading-relaxed">
                Atlanta, GA, USA
              </p>
            </div>

            {/* Contact details */}
            <div className="bg-white rounded-2xl p-6 shadow-md border border-gray-100 mb-4">
              <h3 className="font-black text-[#1A1A1A] text-lg mb-4 flex items-center gap-2">
                <div className="w-8 h-8 bg-[#C0392B]/10 rounded-lg flex items-center justify-center">
                  <Phone size={16} className="text-[#C0392B]" />
                </div>
                Contact
              </h3>
              <div className="space-y-3">
                <a
                  href="tel:+14699331897"
                  className="flex items-center gap-3 text-gray-600 hover:text-[#C0392B] transition-colors group"
                >
                  <Phone size={16} className="text-[#C0392B] shrink-0" />
                  <span className="text-sm group-hover:underline">+1 469-933-1897</span>
                </a>
                <a
                  href="mailto:contact@spiceroadtruck.com"
                  className="flex items-center gap-3 text-gray-600 hover:text-[#C0392B] transition-colors group"
                >
                  <Mail size={16} className="text-[#C0392B] shrink-0" />
                  <span className="text-sm group-hover:underline">contact@spiceroadtruck.com</span>
                </a>
              </div>

              {/* Social */}
              <div className="flex gap-3 mt-4 pt-4 border-t border-gray-100">
                <a
                  href="https://facebook.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-9 h-9 bg-[#C0392B]/10 hover:bg-[#C0392B] rounded-lg flex items-center justify-center text-[#C0392B] hover:text-white transition-all"
                >
                  <Facebook size={16} />
                </a>
                <a
                  href="https://instagram.com"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-9 h-9 bg-[#C0392B]/10 hover:bg-[#C0392B] rounded-lg flex items-center justify-center text-[#C0392B] hover:text-white transition-all"
                >
                  <Instagram size={16} />
                </a>
              </div>
            </div>

            {/* Opening Hours */}
            <div className="bg-white rounded-2xl p-6 shadow-md border border-gray-100">
              <h3 className="font-black text-[#1A1A1A] text-lg mb-4 flex items-center gap-2">
                <div className="w-8 h-8 bg-[#C0392B]/10 rounded-lg flex items-center justify-center">
                  <Clock size={16} className="text-[#C0392B]" />
                </div>
                Hours
              </h3>
              <div className="space-y-2">
                {[
                  { day: 'Monday – Wednesday', hours: 'Closed' },
                  { day: 'Thursday – Friday', hours: '11:00 AM – 7:00 PM' },
                  { day: 'Saturday', hours: '10:00 AM – 9:00 PM' },
                  { day: 'Sunday', hours: '1:00 PM – 6:00 PM' },
                ].map((item, i) => (
                  <div key={i} className="flex justify-between items-center py-1.5 border-b border-gray-50 last:border-0">
                    <span className="text-gray-600 text-sm">{item.day}</span>
                    <span className={`text-sm font-semibold ${item.hours === 'Closed' ? 'text-gray-400' : 'text-[#C0392B]'}`}>
                      {item.hours}
                    </span>
                  </div>
                ))}
              </div>
            </div>

          </div>

          {/* ── RIGHT — Contact Form ── */}
          <div>
            <div className="bg-white rounded-2xl p-8 shadow-md border border-gray-100">

              {submitted ? (
                /* Success state */
                <div className="flex flex-col items-center justify-center py-16 text-center">
                  <div className="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center mb-6">
                    <CheckCircle size={40} className="text-green-500" />
                  </div>
                  <h3 className="text-2xl font-black text-[#1A1A1A] mb-2">Message Sent!</h3>
                  <p className="text-gray-500 mb-6 max-w-sm">
                    Thanks for reaching out! We will get back to you within 24 hours.
                  </p>
                  <button
                    onClick={() => { setSubmitted(false); setForm({ firstName: '', lastName: '', email: '', message: '' }); }}
                    className="bg-[#C0392B] hover:bg-[#E67E22] text-white px-6 py-2 rounded-full font-semibold transition-colors"
                  >
                    Send Another
                  </button>
                </div>
              ) : (
                /* Form */
                <form onSubmit={handleSubmit} className="space-y-5">
                  <div>
                    <h2 className="text-2xl font-black text-[#1A1A1A] mb-1">Send us a message</h2>
                    <p className="text-gray-400 text-sm">We typically respond within 24 hours.</p>
                  </div>

                  {/* Name row */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        First Name
                      </label>
                      <input
                        type="text"
                        name="firstName"
                        value={form.firstName}
                        onChange={handleChange}
                        required
                        placeholder="Akhil"
                        className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:border-[#C0392B] focus:ring-1 focus:ring-[#C0392B] text-gray-800 text-sm transition-colors"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">
                        Last Name
                      </label>
                      <input
                        type="text"
                        name="lastName"
                        value={form.lastName}
                        onChange={handleChange}
                        required
                        placeholder="Gollapalli"
                        className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:border-[#C0392B] focus:ring-1 focus:ring-[#C0392B] text-gray-800 text-sm transition-colors"
                      />
                    </div>
                  </div>

                  {/* Email */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      name="email"
                      value={form.email}
                      onChange={handleChange}
                      required
                      placeholder="you@example.com"
                      className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:border-[#C0392B] focus:ring-1 focus:ring-[#C0392B] text-gray-800 text-sm transition-colors"
                    />
                  </div>

                  {/* Message */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-1">
                      Message
                    </label>
                    <textarea
                      name="message"
                      value={form.message}
                      onChange={handleChange}
                      required
                      rows={5}
                      placeholder="Tell us about your event, questions, or feedback..."
                      className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:border-[#C0392B] focus:ring-1 focus:ring-[#C0392B] text-gray-800 text-sm transition-colors resize-none"
                    />
                  </div>

                  {/* Submit */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-[#C0392B] hover:bg-[#E67E22] disabled:opacity-60 text-white py-3 rounded-xl font-bold text-base transition-colors flex items-center justify-center gap-2"
                  >
                    {loading ? (
                      <>
                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        Sending...
                      </>
                    ) : (
                      <>
                        <Send size={18} /> Send Message
                      </>
                    )}
                  </button>

                </form>
              )}

            </div>
          </div>

        </div>
      </div>

    </div>
  );
}