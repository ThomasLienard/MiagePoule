/**
 * Formate un résultat selon le type de score de l'épreuve.
 *
 * @param {number|null} value  - La valeur brute (secondes pour TIME, points pour POINTS)
 * @param {string}      scoreType - "TIME" | "POINTS"
 * @returns {string}
 */
export const formatScore = (value, scoreType) => {
    if (value === null || value === undefined) return '-';

    if (scoreType === 'POINTS') {
        return `${value} pts`;
    }

    if (scoreType === 'TIME') {
        // value est en secondes (décimales = millisecondes)
        const totalMs = Math.round(value * 1000);
        const ms = totalMs % 1000;
        const totalSeconds = Math.floor(totalMs / 1000);
        const seconds = totalSeconds % 60;
        const totalMinutes = Math.floor(totalSeconds / 60);
        const minutes = totalMinutes % 60;
        const hours = Math.floor(totalMinutes / 60);

        const parts = [
            ...(hours > 0 ? [`${hours}h`] : []),
            ...(minutes > 0 || hours > 0 ? [`${String(minutes).padStart(2, '0')}min`] : []),
            `${String(seconds).padStart(2, '0')}s`,
            `${String(ms).padStart(3, '0')}ms`,
        ];

        return parts.join(' ');
    }

    return String(value);
};
