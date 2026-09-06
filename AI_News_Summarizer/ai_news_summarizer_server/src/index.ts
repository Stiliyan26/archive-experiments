import express from 'express';
import cors from 'cors';
import config from './config/config.js';
import newsRoutes from './routes/newsRoutes.js';

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Health check route
app.get('/', (req, res) => {
  res.json({ 
    message: 'AI News Summarizer API', 
    status: 'running' 
  });
});

app.use('/api/news', newsRoutes);

// Start server
app.listen(config.port, () => {
  console.log(`Server running on http://localhost:${config.port}`);
  console.log(`Environment: ${config.nodeEnv}`);
});