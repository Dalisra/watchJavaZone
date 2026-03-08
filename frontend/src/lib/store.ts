import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface LanguageState {
    lang: 'en' | 'no';
    setLang: (lang: 'en' | 'no') => void;
    toggleLang: () => void;
}

export const useLanguageStore = create<LanguageState>()(
    persist(
        (set) => ({
            lang: 'en',
            setLang: (lang) => set({ lang }),
            toggleLang: () => set((state) => ({ lang: state.lang === 'en' ? 'no' : 'en' })),
        }),
        {
            name: 'javazone-lang-storage',
        }
    )
);
