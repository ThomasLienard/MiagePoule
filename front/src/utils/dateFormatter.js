export const formatDate = (start, end) => {
    if (!start) return '';

    const startDate = new Date(start);
    const endDate = end ? new Date(end) : null;

    const formatTime = (date) => {
        return date.toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    if (endDate && startDate.toDateString() === endDate.toDateString()) {
        return `${startDate.toLocaleDateString()} ${formatTime(startDate)} - ${formatTime(endDate)}`;
    } else if (endDate) {
        return `${startDate.toLocaleDateString()} ${formatTime(startDate)} - ${endDate.toLocaleDateString()} ${formatTime(endDate)}`;
    } else {
        return `${startDate.toLocaleDateString()} ${formatTime(startDate)}`;
    }
};

export const getRelativeTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (date - now) / (1000 * 60 * 60);

    if (diffInHours < 0) {
        return 'Past event';
    } else if (diffInHours < 24) {
        return 'Today';
    } else if (diffInHours < 48) {
        return 'Tomorrow';
    } else if (diffInHours < 168) {
        const days = Math.floor(diffInHours / 24);
        return `In ${days} day${days > 1 ? 's' : ''}`;
    } else {
        return date.toLocaleDateString();
    }
};