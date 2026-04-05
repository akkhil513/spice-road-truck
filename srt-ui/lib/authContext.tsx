'use client';

import { createContext, useContext, useEffect, useState } from 'react';

interface AuthContextType {
    isAdmin: boolean;
    loading: boolean;
    logout: () => Promise<void>;
    refreshAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
    isAdmin: false,
    loading: true,
    logout: async () => {},
    refreshAuth: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [isAdmin, setIsAdmin] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Only run in browser
        if (typeof window === 'undefined') return;
        
        const timer = setTimeout(async () => {
            try {
                const { fetchAuthSession } = await import('aws-amplify/auth');
                await import('./auth');
                const session = await fetchAuthSession();
                setIsAdmin(!!session.tokens);
            } catch {
                setIsAdmin(false);
            } finally {
                setLoading(false);
            }
        }, 100);
        return () => clearTimeout(timer);
    }, []);

    const refreshAuth = async () => {
        try {
            const { fetchAuthSession } = await import('aws-amplify/auth');
            const session = await fetchAuthSession();
            setIsAdmin(!!session.tokens);
        } catch {
            setIsAdmin(false);
        }
    };

    const logout = async () => {
        const { signOut } = await import('aws-amplify/auth');
        await signOut();
        setIsAdmin(false);
    };

    return (
        <AuthContext.Provider value={{ isAdmin, loading, logout, refreshAuth }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);