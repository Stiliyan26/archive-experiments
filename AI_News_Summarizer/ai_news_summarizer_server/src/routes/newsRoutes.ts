import express from 'express';  
import { getTopHeadlines, searchNews } from '../controllers/newsController.js';

const router = express.Router();
router.get('/headlines', getTopHeadlines);
router.get('/search', searchNews);

export default router;