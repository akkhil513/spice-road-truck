'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import { fetchAuthSession, signOut } from 'aws-amplify/auth';
import './auth';

interface AuthContextType {
    isAdmin: boolean;
    loading: boolean;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
    isAdmin: false,
    loading: true,
    logout: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [isAdmin, setIsAdmin] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        checkSession();
    }, []);

    const checkSession = async () => {
        try {
            const session = await fetchAuthSession();
            setIsAdmin(!!session.tokens);
        } catch {
            setIsAdmin(false);
        } finally {
            setLoading(false);
        }
    };

    const logout = async () => {
        await signOut();
        setIsAdmin(false);
    };

    return (
        <AuthContext.Provider value={{ isAdmin, loading, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);