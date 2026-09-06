import type { Request, Response} from 'express';
import config from '../config/config.js';
import type { 
  NewsApiResponse, 
  TopHeadlinesQuery, 
  ApiResponse,
  SearchNewsQuery, 
  SummarizedNewsResponse
} from '../types/news.types.js';
import { summarizeNews } from '../service/summarizeService.js';

const { newsApiKey, newsApiBaseUrl } = config;

const fetchNewsApi = async (endpoint: string, params: Record<string, string>): Promise<NewsApiResponse> => {

  if (!newsApiKey) {
    throw new Error('News API key is not configured.');
  }

  const urlParams = new URLSearchParams(params);

  const response = await fetch(`${newsApiBaseUrl}/${endpoint}?${urlParams.toString()}`, {
    method: 'GET',
    headers: {
      'X-Api-Key': newsApiKey,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || `API error: ${response.status}`);
  }

  return response.json();
}

export const getTopHeadlines = async (
  req: Request<{}, {}, {}, TopHeadlinesQuery>,
  res: Response<ApiResponse<NewsApiResponse>>
): Promise<void> => {
  try {
    const { country = 'us', category } = req.query;

    const params: Record<string, string> = { country };

    if (category) {
      params.category = category;
    }

    const data = await fetchNewsApi('/top-headlines', params);

    res.json({ success: true, data });

  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Failed to fetch news.',
      error: error instanceof Error 
        ? error.message 
        : 'Unknown error'  
    });
  }
}

export const searchNews = async (
  req: Request<{}, {}, {}, SearchNewsQuery>, 
  res: Response<ApiResponse<SummarizedNewsResponse>>
): Promise<void> => {
  try {
    const { q, from, to, sortBy = 'publishedAt' } = req.query;

    if (!q) {
      res.status(400).json({
        success: false,
        message: 'Search query is required'
      });

      return;
    }

    const params: Record<string, string> = { q, sortBy };

    if (from) {
      params.from = from;
    }

    if (to) {
      params.to = to
    }

    const data = await fetchNewsApi('/everything', params);

    const summary = await summarizeNews(data.articles.slice(0, 10));

    res.json({ 
      success: true, 
      data: { 
        ...data, 
        summary 
      } 
    });

  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Failed to search news.',
      error: error instanceof Error 
        ? error.message 
        : 'Unknown error'  
    });
  }
}