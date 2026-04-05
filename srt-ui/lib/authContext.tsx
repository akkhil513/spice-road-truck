'use client';

import { createContext, useContext, useEffect, useState } from 'react';

interface AuthContextType {
    isAdmin: boolean;
    loading: boolean;
    userName: string;
    logout: () => Promise<void>;
    refreshAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
    isAdmin: false,
    loading: true,
    userName: '',
    logout: async () => { },
    refreshAuth: async () => { },
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [isAdmin, setIsAdmin] = useState(false);
    const [loading, setLoading] = useState(true);
    const [userName, setUserName] = useState('');

    useEffect(() => {
        if (typeof window === 'undefined') return;
        const timer = setTimeout(async () => {
            try {
                const { fetchAuthSession, getCurrentUser } = await import('aws-amplify/auth');
                await import('./auth');
                const session = await fetchAuthSession();
                if (session.tokens) {
                    setIsAdmin(true);
                    try {
                        const user = await getCurrentUser();
                        setUserName(user.signInDetails?.loginId || user.username || 'Admin');
                    } catch {
                        setUserName('Admin');
                    }
                }
            } catch {
                setIsAdmin(false);
                setUserName('');
            } finally {
                setLoading(false);
            }
        }, 100);
        return () => clearTimeout(timer);
    }, []);

    const refreshAuth = async () => {
        try {
            const { fetchAuthSession, getCurrentUser } = await import('aws-amplify/auth');
            const session = await fetchAuthSession();
            if (session.tokens) {
                setIsAdmin(true);
                try {
                    const user = await getCurrentUser();
                    setUserName(user.username || user.signInDetails?.loginId || 'Admin');
                } catch {
                    setUserName('Admin');
                }
            }
        } catch {
            setIsAdmin(false);
            setUserName('');
        }
    };

    const logout = async () => {
        const { signOut } = await import('aws-amplify/auth');
        await signOut();
        setIsAdmin(false);
        setUserName('');
    };

    return (
        <AuthContext.Provider value={{ isAdmin, loading, userName, logout, refreshAuth }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);