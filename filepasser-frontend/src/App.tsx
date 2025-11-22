import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AppBar, Toolbar, Typography } from "@mui/material";
import Dashboard from "./pages/Dashboard";
import UploadPage from "./pages/UploadPage";

export default function App() {
    return (
        <BrowserRouter>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6">FilePasser</Typography>
                </Toolbar>
            </AppBar>

            <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/upload" element={<UploadPage />} />
            </Routes>
        </BrowserRouter>
    );
}
