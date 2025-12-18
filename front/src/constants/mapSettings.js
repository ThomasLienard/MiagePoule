// Tailles responsive pour la carte
export const MAP_CONTAINER_STYLE = {
    width: "100%",
    height: "calc(100vh - 200px)",
    minHeight: "500px",
    maxHeight: "800px",
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

export const LOADING_STATES = {
    IDLE: 'idle',
    LOADING: 'loading',
    SUCCESS: 'success',
    ERROR: 'error',
};

export const BREAKPOINTS = {
    MOBILE: 576,
    TABLET: 768,
    DESKTOP: 992,
    LARGE_DESKTOP: 1200,
};