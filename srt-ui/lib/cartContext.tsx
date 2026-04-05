'use client';

import { createContext, useContext, useEffect, useState } from 'react';

export interface CartItem {
    id: string;
    name: string;
    price: number;
    quantity: number;
    category: string;
}

interface CartContextType {
    items: CartItem[];
    addItem: (item: Omit<CartItem, 'quantity'>) => void;
    removeItem: (id: string) => void;
    updateQuantity: (id: string, quantity: number) => void;
    clearCart: () => void;
    total: number;
    totalItems: number;
}

const CartContext = createContext<CartContextType>({
    items: [],
    addItem: () => {},
    removeItem: () => {},
    updateQuantity: () => {},
    clearCart: () => {},
    total: 0,
    totalItems: 0,
});

export function CartProvider({ children }: { children: React.ReactNode }) {
    const [items, setItems] = useState<CartItem[]>([]);

    useEffect(() => {
        const saved = localStorage.getItem('srt-cart');
        if (saved) setItems(JSON.parse(saved));
    }, []);

    const save = (newItems: CartItem[]) => {
        setItems(newItems);
        localStorage.setItem('srt-cart', JSON.stringify(newItems));
    };

    const addItem = (item: Omit<CartItem, 'quantity'>) => {
        const existing = items.find(i => i.id === item.id);
        if (existing) {
            save(items.map(i => i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i));
        } else {
            save([...items, { ...item, quantity: 1 }]);
        }
    };

    const removeItem = (id: string) => {
        save(items.filter(i => i.id !== id));
    };

    const updateQuantity = (id: string, quantity: number) => {
        if (quantity <= 0) {
            removeItem(id);
        } else {
            save(items.map(i => i.id === id ? { ...i, quantity } : i));
        }
    };

    const clearCart = () => {
        setItems([]);
        localStorage.removeItem('srt-cart');
    };

    const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);
    const totalItems = items.reduce((sum, i) => sum + i.quantity, 0);

    return (
        <CartContext.Provider value={{ items, addItem, removeItem, updateQuantity, clearCart, total, totalItems }}>
            {children}
        </CartContext.Provider>
    );
}

export const useCart = () => useContext(CartContext);