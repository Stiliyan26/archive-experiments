import * as Joi from 'joi';

export const validationSchema = Joi.object({
  API_PORT: Joi.number().port().default(3001),
  API_CORS_ORIGINS: Joi.string().allow('').default('http://localhost:4200'),
  MONGODB_URI: Joi.string()
    .uri({ scheme: ['mongodb', 'mongodb+srv'] })
    .default('mongodb://localhost:27017/life-patterns'),
});
