import * as dotenv from 'dotenv';
import express from 'express';
import cors from 'cors';

dotenv.config();

if (!process.env.PORT) {
  process.exit(1);
}