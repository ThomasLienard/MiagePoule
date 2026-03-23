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

    if (startDate.toDateString() === endDate?.toDateString()) {
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

    const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfTargetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const diffInDays = Math.round((startOfTargetDay - startOfToday) / (1000 * 60 * 60 * 24));

    if (diffInDays < 0) {
        return 'Past event';
    } else if (diffInDays === 0) {
        return 'Today';
    } else if (diffInDays === 1) {
        return 'Tomorrow';
    } else if (diffInDays < 7) {
        const days = diffInDays;
        return `In ${days} day${days > 1 ? 's' : ''}`;
    } else {
        return date.toLocaleDateString();
    }
};

export const isPastEvent = (item) => {
    const eventDate = item.timeSlot?.end || item.date || item.endDate;
    if (!eventDate) return false;

    const today = new Date();

    const itemDate = new Date(eventDate);

    return itemDate < today;
};