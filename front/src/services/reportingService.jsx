import axios from 'axios';

const API_BASE_URL = 'http://localhost:8084';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
    };
};

const reportingService = {
    getMetrics: async (period, date) => {
        const query = new URLSearchParams({ period });
        if (date) {
            query.append('date', date);
        }

        const response = await axios.get(
            `${API_BASE_URL}/admin/reporting/metrics?${query.toString()}`,
            { headers: getAuthHeaders() }
        );

        return response.data;
    }
};

export default reportingService;
