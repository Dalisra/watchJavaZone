import { Outlet, createRootRoute } from '@tanstack/react-router'

export const Route = createRootRoute({
    component: () => (
        <div className="min-h-screen bg-gray-50 text-gray-900">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
                    <h1 className="text-3xl font-bold text-gray-900">Watch JavaZone</h1>
                </div>
            </header>
            <main>
                <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
                    <Outlet />
                </div>
            </main>
        </div>
    ),
})
