import axios from "axios";

const API_URL = import.meta.env.VITE_UPLOAD_ENDPOINT ?? "http://localhost:8081/api/upload";

export function uploadFile(file: File, clientId: string) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("clientId", clientId);

    return axios.post(API_URL, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
}
