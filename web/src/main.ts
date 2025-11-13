import { FrameViewer, FrameStats } from './FrameViewer.js';

/**
 * Application class for managing the edge detection web viewer
 */
class EdgeDetectionApp {
    private frameViewer: FrameViewer;
    private apiEndpoint = 'http://localhost:8081/api/frame';
    private refreshInterval = 1000; // 1 second
    private intervalId: number | null = null;
    private frameCount = 0;

    constructor() {
        // Get canvas element
        const canvas = document.getElementById('frameCanvas') as HTMLCanvasElement;
        if (!canvas) {
            throw new Error('Canvas element not found');
        }

        // Initialize frame viewer
        this.frameViewer = new FrameViewer(canvas);

        // Setup event listeners
        this.setupEventListeners();

        // Load initial sample frame
        this.loadSampleFrame();
    }

    /**
     * Setup UI event listeners
     */
    private setupEventListeners(): void {
        const startBtn = document.getElementById('startBtn');
        const stopBtn = document.getElementById('stopBtn');
        const exportBtn = document.getElementById('exportBtn');
        const refreshRateInput = document.getElementById('refreshRate') as HTMLInputElement;

        if (startBtn) {
            startBtn.addEventListener('click', () => this.startFetching());
        }

        if (stopBtn) {
            stopBtn.addEventListener('click', () => this.stopFetching());
        }

        if (exportBtn) {
            exportBtn.addEventListener('click', () => this.exportFrame());
        }

        if (refreshRateInput) {
            refreshRateInput.addEventListener('change', (e) => {
                const target = e.target as HTMLInputElement;
                this.refreshInterval = parseInt(target.value, 10);
                console.log(`Refresh interval set to ${this.refreshInterval}ms`);
            });
        }
    }

    /**
     * Load a sample frame for demonstration
     */
    private loadSampleFrame(): void {
        // Create a sample processed frame (solid color for demonstration)
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d');

        if (ctx) {
            // Draw gradient background
            const gradient = ctx.createLinearGradient(0, 0, 640, 480);
            gradient.addColorStop(0, '#000000');
            gradient.addColorStop(0.5, '#1a4d1a');
            gradient.addColorStop(1, '#000000');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, 640, 480);

            // Draw some edge-like patterns
            ctx.strokeStyle = '#00FF00';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.rect(50, 50, 540, 380);
            ctx.stroke();

            // Draw some synthetic edges
            for (let i = 0; i < 5; i++) {
                ctx.strokeStyle = `rgb(0, ${255 - i * 30}, 0)`;
                ctx.lineWidth = 1;
                ctx.beginPath();
                ctx.arc(320 + i * 20 - 40, 240 + i * 10 - 20, 30 + i * 5, 0, Math.PI * 2);
                ctx.stroke();
            }
        }

        const imageUrl = canvas.toDataURL('image/png');

        // Update frame with sample stats
        const sampleStats: FrameStats = {
            fps: 24,
            processingTime: 15.5,
            resolution: '640x480',
            frameCount: 100
        };

        this.frameViewer.updateFrame(imageUrl, sampleStats);
        console.log('Sample frame loaded');
    }

    /**
     * Start fetching frames from the backend
     */
    private startFetching(): void {
        if (this.intervalId !== null) {
            console.warn('Already fetching frames');
            return;
        }

        console.log('Starting frame fetching');
        this.fetchFrame(); // Fetch immediately
        this.intervalId = window.setInterval(() => this.fetchFrame(), this.refreshInterval);
    }

    /**
     * Stop fetching frames
     */
    private stopFetching(): void {
        if (this.intervalId !== null) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            console.log('Stopped frame fetching');
        }
    }

    /**
     * Fetch a frame from the backend
     */
    private async fetchFrame(): Promise<void> {
        try {
            const response = await fetch(this.apiEndpoint);

            if (!response.ok) {
                console.warn(`API returned status ${response.status}`);
                return;
            }

            const data = await response.json();

            if (data.frameData) {
                const imageUrl = `data:image/png;base64,${data.frameData}`;

                const stats: FrameStats = {
                    fps: data.fps || 0,
                    processingTime: data.processingTime || 0,
                    resolution: data.resolution || '0x0',
                    frameCount: ++this.frameCount
                };

                this.frameViewer.updateFrame(imageUrl, stats);
            }
        } catch (error) {
            console.warn('Failed to fetch frame:', error);
            // Continue attempting to fetch despite errors
        }
    }

    /**
     * Export current frame
     */
    private exportFrame(): void {
        const dataUrl = this.frameViewer.exportFrame();
        const link = document.createElement('a');
        link.href = dataUrl;
        link.download = `edge-detection-${Date.now()}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        console.log('Frame exported');
    }
}

/**
 * Initialize application when DOM is ready
 */
document.addEventListener('DOMContentLoaded', () => {
    try {
        const app = new EdgeDetectionApp();
        console.log('Application initialized');
    } catch (error) {
        console.error('Failed to initialize application:', error);
        alert('Failed to initialize application. Check console for details.');
    }
});
