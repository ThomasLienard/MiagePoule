import { useState } from 'react';

/**
 * Hook personnalisé pour gérer les formulaires de mot de passe
 * Utilisé par ChangePasswordPage et ActivateAccountPage
 */
export const usePasswordForm = (onSubmit, additionalValidation = null) => {
    const [formData, setFormData] = useState({
        newPassword: '',
        confirmPassword: ''
    });
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const validate = () => {
        if (!formData.newPassword) {
            return 'Le mot de passe est requis';
        }
        if (formData.newPassword.length < 6) {
            return 'Le mot de passe doit contenir au moins 6 caractères';
        }
        if (formData.newPassword !== formData.confirmPassword) {
            return 'Les mots de passe ne correspondent pas';
        }
        // Validation supplémentaire personnalisée
        if (additionalValidation) {
            const additionalError = additionalValidation();
            if (additionalError) return additionalError;
        }
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setLoading(true);
        try {
            await onSubmit(formData.newPassword);
            setSuccess(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return {
        formData,
        error,
        success,
        loading,
        handleChange,
        handleSubmit,
        setError,
        setSuccess
    };
};

export default usePasswordForm;
