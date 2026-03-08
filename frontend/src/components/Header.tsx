import { Link, useNavigate } from '@tanstack/react-router';
import { Search, Menu, Video, Globe } from 'lucide-react';
import { useState } from 'react';
import { useLanguageStore } from '../lib/store';
import { useQueryClient } from '@tanstack/react-query';

interface HeaderProps {
    onMenuClick: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
    const [search, setSearch] = useState('');
    const navigate = useNavigate();

    // Global language state
    const { lang, toggleLang } = useLanguageStore();
    const queryClient = useQueryClient();

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (search.trim()) {
            navigate({ to: '/search', search: { q: search.trim() } });
        }
    };

    const handleLanguageToggle = () => {
        toggleLang();
        // Force all cached 'videos' queries to refetch instantly with the new ?lang parameter
        queryClient.invalidateQueries({ queryKey: ['videos'] });
        queryClient.invalidateQueries({ queryKey: ['video'] });
    };

    return (
        <header className="sticky top-0 z-50 flex h-16 items-center justify-between bg-background px-4 lg:px-6">
            <div className="flex items-center gap-4">
                <button
                    onClick={onMenuClick}
                    className="p-2 -ml-2 rounded-full hover:bg-surface-hover text-text-primary"
                >
                    <Menu className="h-6 w-6" />
                </button>
                <Link to="/" className="flex items-center gap-1 text-xl font-bold tracking-tight">
                    <div className="bg-brand-500 text-white p-1 rounded-lg">
                        <Video className="h-5 w-5" />
                    </div>
                    <span>JavaZone<span className="font-light">Watch</span></span>
                </Link>
            </div>

            <div className="flex flex-1 items-center justify-center max-w-2xl px-8 hidden sm:flex">
                <form
                    onSubmit={handleSearch}
                    className="flex w-full items-center overflow-hidden rounded-full border border-surface-hover bg-surface-hover/50 focus-within:border-brand-500 focus-within:bg-background"
                >
                    <div className="pl-4 text-text-secondary">
                        <Search className="h-4 w-4" />
                    </div>
                    <input
                        type="text"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Search talks, speakers, or topics..."
                        className="w-full bg-transparent px-3 py-2 text-sm outline-none placeholder:text-text-secondary"
                    />
                </form>
            </div>

            {/* Mobile search toggle could go here, for now keeping it simple */}
            <div className="flex items-center gap-4 w-auto">
                <button
                    onClick={handleLanguageToggle}
                    className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-hover hover:bg-surface text-sm font-medium transition-colors"
                    title="Toggle Language"
                >
                    <Globe className="h-4 w-4 text-brand-500" />
                    <span className="uppercase">{lang}</span>
                </button>
                {/* User avatar or settings placeholder */}
            </div>
        </header>
    );
}
