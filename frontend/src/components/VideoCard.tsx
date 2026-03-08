import { Link } from '@tanstack/react-router';
import { formatDistanceToNow } from 'date-fns';
import type { VideoResponse } from '../lib/api';
import { Play } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';

interface VideoCardProps {
    video: VideoResponse;
}

export function VideoCard({ video }: VideoCardProps) {
    const { data: thumbnailUrl } = useQuery({
        queryKey: ['vimeo-thumbnail', video.vimeoId],
        queryFn: async () => {
            if (!video.vimeoId) return null;
            try {
                const res = await fetch(`https://vimeo.com/api/oembed.json?url=https://vimeo.com/${video.vimeoId}`);
                if (!res.ok) return null;
                const data = await res.json();
                return data.thumbnail_url as string || null;
            } catch (e) {
                return null;
            }
        },
        staleTime: 1000 * 60 * 60 * 24, // cache for 24 hours
        gcTime: 1000 * 60 * 60 * 24,
    });

    const formattedLength = video.lengthMinutes
        ? `${video.lengthMinutes} min`
        : '';

    const formattedDate = video.startTime
        ? formatDistanceToNow(new Date(video.startTime), { addSuffix: true })
        : '';

    return (
        <Link
            to="/video/$id"
            params={{ id: video.id }}
            className="group flex flex-col gap-3 rounded-xl cursor-pointer"
        >
            <div className="relative aspect-video w-full overflow-hidden rounded-xl bg-surface">
                {thumbnailUrl ? (
                    <img
                        src={thumbnailUrl}
                        alt=""
                        loading="lazy"
                        className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    />
                ) : (
                    <div className="absolute inset-0 bg-gradient-to-br from-surface to-surface-hover group-hover:scale-105 transition-transform duration-300 flex items-center justify-center">
                        <Play className="w-12 h-12 text-zinc-600 group-hover:text-brand-500 transition-colors" />
                    </div>
                )}

                {/* Duration badge */}
                {formattedLength && (
                    <div className="absolute bottom-2 right-2 bg-black/80 text-white text-xs font-medium px-1.5 py-0.5 rounded">
                        {formattedLength}
                    </div>
                )}
            </div>

            <div className="flex gap-3">
                <div className="flex flex-col">
                    <h3 className="text-sm font-semibold text-text-primary line-clamp-2 leading-tight group-hover:text-brand-500 transition-colors">
                        {video.title}
                    </h3>

                    <div className="text-xs text-text-secondary mt-1 flex flex-col gap-0.5">
                        {video.speakers && video.speakers.length > 0 && (
                            <span className="truncate">{video.speakers.map(s => s.name).join(', ')}</span>
                        )}
                        <div className="flex justify-between gap-1.5 text-[11px] text-text-secondary">
                            <span>{video.viewCount || 0} views</span>
                            {formattedDate && (
                                <>
                                    <span>{formattedDate}</span>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </Link>
    );
}
