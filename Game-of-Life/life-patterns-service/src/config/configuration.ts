export const configuration = () => {
  const defaultOrigins = ['http://localhost:4200'];
  const rawOrigins = process.env.API_CORS_ORIGINS ?? '';
  const origins = rawOrigins
    .split(',')
    .map((origin) => origin.trim())
    .filter(Boolean);

  return {
    app: {
      port: parseInt(process.env.API_PORT ?? '3001', 10),
      corsOrigins: origins.length > 0 ? origins : defaultOrigins,
    },
    database: {
      uri: process.env.MONGODB_URI ?? 'mongodb://localhost:27017/life-patterns',
    },
  };
};

export type AppConfig = ReturnType<typeof configuration>;

export default configuration;
