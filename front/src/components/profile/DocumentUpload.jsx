import React, { useState } from "react";
import PropTypes from "prop-types";
import {
  Form,
  Button,
  Card,
  Alert,
  Spinner,
  ProgressBar,
} from "react-bootstrap";
import { Upload, File, CheckCircle, XCircle } from "lucide-react";
import documentService from "../../services/documentService";

const DocumentUpload = ({
  documentType,
  label,
  description,
  onUploadSuccess,
  existingDocument,
}) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [message, setMessage] = useState({ type: "", text: "" });
  const [preview, setPreview] = useState(null);

  const getDocumentStatus = (doc) => doc?.status ?? doc?.validationStatus ?? doc?.state;
  const existingStatus = getDocumentStatus(existingDocument);
  const isExistingValidated = existingStatus === "VALIDATED";

  // Types de documents autorisés
  const allowedTypes = {
    CEN_ACCREDITATION: { accept: ".pdf,.jpg,.jpeg,.png", maxSize: 10 },
    PASSPORT: { accept: ".pdf,.jpg,.jpeg,.png", maxSize: 10 },
    MEDICAL_CERTIFICATE: { accept: ".pdf,.jpg,.jpeg,.png", maxSize: 10 },
  };

  const config = allowedTypes[documentType] || { accept: ".pdf", maxSize: 10 };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Vérifier la taille
    const maxSizeBytes = config.maxSize * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      setMessage({
        type: "danger",
        text: `Le fichier est trop volumineux. Taille maximale : ${config.maxSize} MB`,
      });
      return;
    }

    // Vérifier le type
    const fileExtension = "." + file.name.split(".").pop().toLowerCase();
    if (!config.accept.includes(fileExtension)) {
      setMessage({
        type: "danger",
        text: `Format de fichier non autorisé. Formats acceptés : ${config.accept}`,
      });
      return;
    }

    setSelectedFile(file);
    setMessage({ type: "", text: "" });

    // Générer un aperçu pour les images
    if (file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result);
      };
      reader.readAsDataURL(file);
    } else {
      setPreview(null);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setMessage({ type: "warning", text: "Veuillez sélectionner un fichier" });
      return;
    }

    setUploading(true);
    setUploadProgress(0);

    try {
      // Simuler la progression
      const progressInterval = setInterval(() => {
        setUploadProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 200);

      const response = await documentService.uploadDocument(
        selectedFile,
        documentType,
        description,
      );

      clearInterval(progressInterval);
      setUploadProgress(100);

      setMessage({
        type: "success",
        text: "Document téléversé avec succès et chiffré en toute sécurité",
      });

      // Réinitialiser après succès
      setTimeout(() => {
        setSelectedFile(null);
        setPreview(null);
        setUploadProgress(0);
        if (onUploadSuccess) {
          onUploadSuccess(response);
        }
      }, 1500);
    } catch (error) {
      setMessage({
        type: "danger",
        text:
          error.response?.data?.message ||
          "Erreur lors du téléversement du document",
      });
      setUploadProgress(0);
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = () => {
    setSelectedFile(null);
    setPreview(null);
    setMessage({ type: "", text: "" });
  };

  return (
    <Card className="mb-3 shadow-sm">
      <Card.Body>
        <div className="d-flex align-items-center mb-2">
          <Upload size={20} className="me-2 text-secondary" />
          <h6 className="mb-0">{label}</h6>
        </div>

        {description && <p className="text-muted small mb-3">{description}</p>}

        {message.text && (
          <Alert
            variant={message.type}
            dismissible
            onClose={() => setMessage({ type: "", text: "" })}
            className="py-2"
          >
            {message.text}
          </Alert>
        )}

        {isExistingValidated && !selectedFile && (
          <Alert variant="success" className="py-2 d-flex align-items-center">
            <CheckCircle size={18} className="me-2" />
            <span>Document déjà téléversé</span>
          </Alert>
        )}

        {existingDocument && !isExistingValidated && !selectedFile && (
          <Alert variant="warning" className="py-2 d-flex align-items-center">
            <XCircle size={18} className="me-2" />
            <span>
              Document déposé mais non validé. Vous pouvez le remplacer en
              téléversant une nouvelle version.
            </span>
          </Alert>
        )}

        {(!existingDocument || !isExistingValidated) && (
          <>
            <Form.Group className="mb-3">
              <Form.Control
                type="file"
                accept={config.accept}
                onChange={handleFileSelect}
                disabled={uploading}
                className="mb-2"
              />
              <Form.Text className="text-muted">
                Formats acceptés : {config.accept} | Taille max :{" "}
                {config.maxSize} MB
              </Form.Text>
            </Form.Group>

            {selectedFile && (
              <Card className="mb-3 bg-light border">
                <Card.Body className="p-3">
                  <div className="d-flex align-items-center justify-content-between">
                    <div className="d-flex align-items-center flex-grow-1">
                      <File size={24} className="me-2 text-primary" />
                      <div className="flex-grow-1">
                        <div className="fw-medium">{selectedFile.name}</div>
                        <small className="text-muted">
                          {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                        </small>
                      </div>
                    </div>
                    {!uploading && (
                      <Button
                        variant="link"
                        size="sm"
                        onClick={handleRemove}
                        className="text-danger p-0"
                      >
                        <XCircle size={20} />
                      </Button>
                    )}
                  </div>

                  {preview && (
                    <div className="mt-2">
                      <img
                        src={preview}
                        alt="Aperçu"
                        style={{
                          maxWidth: "100%",
                          maxHeight: "200px",
                          objectFit: "contain",
                        }}
                        className="rounded"
                      />
                    </div>
                  )}
                </Card.Body>
              </Card>
            )}

            {uploading && uploadProgress > 0 && (
              <ProgressBar
                now={uploadProgress}
                label={`${uploadProgress}%`}
                animated
                className="mb-3"
              />
            )}

            <div className="d-flex gap-2">
              <Button
                variant="primary"
                onClick={handleUpload}
                disabled={!selectedFile || uploading}
                className="flex-grow-1"
              >
                {uploading ? (
                  <>
                    <Spinner size="sm" animation="border" className="me-2" />
                    Téléversement...
                  </>
                ) : (
                  "Téléverser le document"
                )}
              </Button>
            </div>
          </>
        )}
      </Card.Body>
    </Card>
  );
};

DocumentUpload.propTypes = {
  documentType: PropTypes.number.isRequired,
  label: PropTypes.string.isRequired,
  description: PropTypes.string,
  onUploadSuccess: PropTypes.func,
  existingDocument: PropTypes.object,
};

DocumentUpload.defaultProps = {
  description: "",
  onUploadSuccess: null,
  existingDocument: null,
};

export default DocumentUpload;
