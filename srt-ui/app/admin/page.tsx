'use client';

import { signIn, confirmSignIn, resetPassword, confirmResetPassword } from 'aws-amplify/auth';
import '../../lib/auth';
import { useState, useEffect } from 'react';

const fieldStyle: React.CSSProperties = {
    background: 'rgba(255,255,255,0.06)',
    border: '1px solid rgba(255,255,255,0.12)',
    borderRadius: '8px',
    padding: '12px 16px',
    color: '#fff',
    fontSize: '14px',
    outline: 'none',
};

const primaryButtonStyle = (loading: boolean): React.CSSProperties => ({
    background: '#C0392B',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    padding: '12px',
    fontSize: '15px',
    fontWeight: 600,
    cursor: loading ? 'not-allowed' : 'pointer',
    opacity: loading ? 0.7 : 1,
});

const linkButtonStyle: React.CSSProperties = {
    background: 'none',
    border: 'none',
    color: 'rgba(255,255,255,0.55)',
    fontSize: '13px',
    cursor: 'pointer',
    padding: 0,
    textAlign: 'center',
};

export default function AdminLogin() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [needsNewPassword, setNeedsNewPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [mode, setMode] = useState<'login' | 'forgot' | 'reset'>('login');
    const [code, setCode] = useState('');
    const [info, setInfo] = useState('');

    useEffect(() => {
        import('aws-amplify/auth').then(({ signOut }) => {
            signOut().catch(() => {});
        });
    }, []);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const result = await signIn({ username: email, password });

            if (result.nextStep.signInStep === 'CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED') {
                setNeedsNewPassword(true);
                setLoading(false);
                return;
            }

            if (!result.isSignedIn) {
                setError(`Sign-in not complete: ${result.nextStep.signInStep}`);
                setLoading(false);
                return;
            }

            window.location.href = '/admin/dashboard';
        } catch (err: any) {
            if (err?.name === 'PasswordResetRequiredException') {
                try {
                    await sendCode();
                } catch (resetErr: any) {
                    setError(resetErr.message || 'Could not send code');
                }
                setLoading(false);
                return;
            }
            setError(err.message || 'Login failed');
            setLoading(false);
        }
    };

    const sendCode = async () => {
        await resetPassword({ username: email });
        setMode('reset');
        setError('');
        setInfo('We emailed you a 6-digit code.');
    };

    const handleForgot = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await sendCode();
        } catch (err: any) {
            setError(err.message || 'Could not send code');
        } finally {
            setLoading(false);
        }
    };

    const handleReset = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await confirmResetPassword({ username: email, confirmationCode: code, newPassword });
            setMode('login');
            setPassword('');
            setCode('');
            setNewPassword('');
            setInfo('Password updated. Please sign in.');
        } catch (err: any) {
            setError(err.message || 'Could not reset password');
        } finally {
            setLoading(false);
        }
    };

    const handleNewPassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await confirmSignIn({ challengeResponse: newPassword });
            window.location.href = '/admin/dashboard';
        } catch (err: any) {
            setError(err.message || 'Failed to set new password');
            setLoading(false);
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            background: '#1A1A1A',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
        }}>
            <div style={{
                background: '#2A2A2A',
                padding: '2.5rem',
                borderRadius: '12px',
                width: '100%',
                maxWidth: '400px',
                border: '1px solid rgba(255,255,255,0.08)',
            }}>
                <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                    <img src="/images/logo/srt-logo-dark.svg" alt="SRT" width={60} height={60} />
                    <h1 style={{ color: '#fff', fontSize: '20px', fontWeight: 600, marginTop: '1rem' }}>
                        Admin Panel
                    </h1>
                    <p style={{ color: 'rgba(255,255,255,0.4)', fontSize: '13px' }}>
                        Spice Road Truck
                    </p>
                </div>

                {mode === 'forgot' ? (
                    <form onSubmit={handleForgot} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '13px', margin: 0 }}>
                            Enter your email and we will send you a code to reset your password.
                        </p>
                        <input type="email" placeholder="Email" value={email}
                            onChange={e => setEmail(e.target.value)} required style={fieldStyle} />
                        {error && <p style={{ color: '#E74C3C', fontSize: '13px', margin: 0 }}>{error}</p>}
                        <button type="submit" disabled={loading} style={primaryButtonStyle(loading)}>
                            {loading ? 'Sending...' : 'Send code'}
                        </button>
                        <button type="button" style={linkButtonStyle}
                            onClick={() => { setMode('login'); setError(''); setInfo(''); }}>
                            Back to sign in
                        </button>
                    </form>
                ) : mode === 'reset' ? (
                    <form onSubmit={handleReset} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {info && <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '13px', margin: 0 }}>{info}</p>}
                        <input type="text" inputMode="numeric" autoComplete="one-time-code" placeholder="6-digit code"
                            value={code} onChange={e => setCode(e.target.value.trim())} required style={fieldStyle} />
                        <input type="password" autoComplete="new-password" placeholder="New password"
                            value={newPassword} onChange={e => setNewPassword(e.target.value)} required style={fieldStyle} />
                        {error && <p style={{ color: '#E74C3C', fontSize: '13px', margin: 0 }}>{error}</p>}
                        <button type="submit" disabled={loading} style={primaryButtonStyle(loading)}>
                            {loading ? 'Resetting...' : 'Reset password'}
                        </button>
                        <button type="button" style={linkButtonStyle}
                            onClick={() => { setMode('login'); setError(''); setInfo(''); }}>
                            Back to sign in
                        </button>
                    </form>
                ) : !needsNewPassword ? (
                    <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {info && <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '13px', margin: 0 }}>{info}</p>}
                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                            style={{
                                background: 'rgba(255,255,255,0.06)',
                                border: '1px solid rgba(255,255,255,0.12)',
                                borderRadius: '8px',
                                padding: '12px 16px',
                                color: '#fff',
                                fontSize: '14px',
                                outline: 'none',
                            }}
                        />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            style={{
                                background: 'rgba(255,255,255,0.06)',
                                border: '1px solid rgba(255,255,255,0.12)',
                                borderRadius: '8px',
                                padding: '12px 16px',
                                color: '#fff',
                                fontSize: '14px',
                                outline: 'none',
                            }}
                        />
                        {error && (
                            <p style={{ color: '#E74C3C', fontSize: '13px', margin: 0 }}>{error}</p>
                        )}
                        <button
                            type="submit"
                            disabled={loading}
                            style={{
                                background: '#C0392B',
                                color: '#fff',
                                border: 'none',
                                borderRadius: '8px',
                                padding: '12px',
                                fontSize: '15px',
                                fontWeight: 600,
                                cursor: loading ? 'not-allowed' : 'pointer',
                                opacity: loading ? 0.7 : 1,
                            }}>
                            {loading ? 'Signing in...' : 'Sign In'}
                        </button>
                        <button type="button" style={linkButtonStyle}
                            onClick={() => { setMode('forgot'); setError(''); setInfo(''); }}>
                            Forgot password?
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleNewPassword} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '13px' }}>
                            Please set a new password to continue.
                        </p>
                        <input
                            type="password"
                            placeholder="New Password"
                            value={newPassword}
                            onChange={e => setNewPassword(e.target.value)}
                            required
                            style={{
                                background: 'rgba(255,255,255,0.06)',
                                border: '1px solid rgba(255,255,255,0.12)',
                                borderRadius: '8px',
                                padding: '12px 16px',
                                color: '#fff',
                                fontSize: '14px',
                                outline: 'none',
                            }}
                        />
                        {error && (
                            <p style={{ color: '#E74C3C', fontSize: '13px', margin: 0 }}>{error}</p>
                        )}
                        <button
                            type="submit"
                            disabled={loading}
                            style={{
                                background: '#C0392B',
                                color: '#fff',
                                border: 'none',
                                borderRadius: '8px',
                                padding: '12px',
                                fontSize: '15px',
                                fontWeight: 600,
                                cursor: loading ? 'not-allowed' : 'pointer',
                            }}>
                            {loading ? 'Setting password...' : 'Set New Password'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}