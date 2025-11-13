/**
 * FrameViewer class for displaying processed frames from edge detection
 */
export class FrameViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private currentFrame: HTMLImageElement | null = null;
    private frameStats: FrameStats = {
        fps: 0,
        processingTime: 0,
        resolution: '0x0',
        frameCount: 0
    };

    constructor(canvasElement: HTMLCanvasElement) {
        this.canvas = canvasElement;
        const context = this.canvas.getContext('2d');
        if (!context) {
            throw new Error('Failed to get 2D context from canvas');
        }
        this.ctx = context;
        this.setupCanvas();
    }

    private setupCanvas(): void {
        // Set canvas size
        this.canvas.width = 640;
        this.canvas.height = 480;

        // Draw initial placeholder
        this.drawPlaceholder();
    }

    /**
     * Update the displayed frame
     */
    public updateFrame(imageSource: string | HTMLImageElement, stats?: Partial<FrameStats>): void {
        if (typeof imageSource === 'string') {
            const img = new Image();
            img.onload = () => {
                this.currentFrame = img;
                this.renderFrame();
            };
            img.onerror = () => {
                console.error('Failed to load image');
                this.drawError('Failed to load image');
            };
            img.src = imageSource;
        } else {
            this.currentFrame = imageSource;
            this.renderFrame();
        }

        // Update stats
        if (stats) {
            this.frameStats = { ...this.frameStats, ...stats };
        }
        this.updateStatsOverlay();
    }

    /**
     * Render the current frame on canvas
     */
    private renderFrame(): void {
        if (!this.currentFrame) {
            this.drawPlaceholder();
            return;
        }

        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Calculate scaling to fit image in canvas
        const scale = Math.min(
            this.canvas.width / this.currentFrame.width,
            this.canvas.height / this.currentFrame.height
        );

        const scaledWidth = this.currentFrame.width * scale;
        const scaledHeight = this.currentFrame.height * scale;
        const x = (this.canvas.width - scaledWidth) / 2;
        const y = (this.canvas.height - scaledHeight) / 2;

        // Draw the frame
        this.ctx.drawImage(this.currentFrame, x, y, scaledWidth, scaledHeight);
    }

    /**
     * Update statistics overlay
     */
    private updateStatsOverlay(): void {
        const statsText = [
            `FPS: ${this.frameStats.fps}`,
            `Processing: ${this.frameStats.processingTime.toFixed(2)}ms`,
            `Resolution: ${this.frameStats.resolution}`,
            `Frame: ${this.frameStats.frameCount}`
        ];

        // Draw semi-transparent background for text
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        this.ctx.fillRect(10, 10, 250, 100);

        // Draw text
        this.ctx.fillStyle = '#00FF00';
        this.ctx.font = 'bold 14px monospace';
        statsText.forEach((text, index) => {
            this.ctx.fillText(text, 20, 30 + index * 20);
        });
    }

    /**
     * Draw placeholder when no frame is available
     */
    private drawPlaceholder(): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctx.fillStyle = '#1a1a1a';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        this.ctx.fillStyle = '#666666';
        this.ctx.font = 'bold 20px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(
            'Waiting for frame...',
            this.canvas.width / 2,
            this.canvas.height / 2
        );
    }

    /**
     * Draw error message
     */
    private drawError(message: string): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctx.fillStyle = '#330000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        this.ctx.fillStyle = '#FF0000';
        this.ctx.font = 'bold 16px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(message, this.canvas.width / 2, this.canvas.height / 2);
    }

    /**
     * Set frame stats
     */
    public setStats(stats: Partial<FrameStats>): void {
        this.frameStats = { ...this.frameStats, ...stats };
        this.updateStatsOverlay();
    }

    /**
     * Get current stats
     */
    public getStats(): FrameStats {
        return { ...this.frameStats };
    }

    /**
     * Export current frame as data URL
     */
    public exportFrame(): string {
        return this.canvas.toDataURL('image/png');
    }
}

/**
 * Interface for frame statistics
 */
export interface FrameStats {
    fps: number;
    processingTime: number;
    resolution: string;
    frameCount: number;
}
