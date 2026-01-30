// Tailles responsive pour la carte
export const MAP_CONTAINER_STYLE = {
    height: "70vh",
};

export const DEFAULT_CENTER = {
    lat: 46.5,
    lng: 2.5,
};

export const DEFAULT_ZOOM = 6;

export const GOOGLE_MAPS_OPTIONS = {
    disableDefaultUI: false,
    zoomControl: true,
    mapTypeControl: true,
    streetViewControl: true,
    fullscreenControl: true,
    scaleControl: true,
    rotateControl: true,
    mapTypeId: 'roadmap',
    styles: [
        {
            featureType: "poi",
            elementType: "labels",
            stylers: [{ visibility: "off" }]
        }
    ]
};