import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../../core/api/api-url.token';
import { CreatePatternPayload, Pattern } from '../models/pattern.models';

@Injectable()
export class PatternService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);
  private readonly resourceUrl = `${this.apiUrl}/patterns`;

  getAll(): Observable<Pattern[]> {
    return this.http.get<Pattern[]>(this.resourceUrl);
  }

  getById(id: string): Observable<Pattern> {
    return this.http.get<Pattern>(`${this.resourceUrl}/${id}`);
  }

  create(payload: CreatePatternPayload): Observable<Pattern> {
    return this.http.post<Pattern>(this.resourceUrl, payload);
  }

  update(id: string, payload: Partial<CreatePatternPayload>): Observable<Pattern> {
    return this.http.put<Pattern>(`${this.resourceUrl}/${id}`, payload);
  }

  delete(id: string): Observable<Pattern> {
    return this.http.delete<Pattern>(`${this.resourceUrl}/${id}`);
  }
}

