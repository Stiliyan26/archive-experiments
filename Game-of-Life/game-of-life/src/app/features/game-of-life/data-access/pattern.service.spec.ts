import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { PatternService } from './pattern.service';
import { API_URL } from '../../../core/api/api-url.token';
import { CreatePatternPayload, Pattern } from '../models/pattern.models';

describe('PatternService', () => {
  let service: PatternService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://test-api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PatternService,
        {
          provide: API_URL,
          useValue: baseUrl,
        },
      ],
    });

    service = TestBed.inject(PatternService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request all patterns', () => {
    const mockResponse: Pattern[] = [];

    service.getAll().subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/patterns`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should request a pattern by id', () => {
    const mockResponse = { id: '1' } as Pattern;

    service.getById('1').subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/patterns/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should create a pattern', () => {
    const payload = {
      name: 'Glider',
      rows: 20,
      cols: 20,
      grid: [[1]],
    } as CreatePatternPayload;
    const mockResponse = { id: '1', ...payload } as Pattern;

    service.create(payload).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/patterns`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should update a pattern', () => {
    const payload: Partial<CreatePatternPayload> = { name: 'Updated' };
    const mockResponse = { id: '1', name: 'Updated' } as Pattern;

    service.update('1', payload).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/patterns/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should delete a pattern', () => {
    const mockResponse = { id: '1' } as Pattern;

    service.delete('1').subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${baseUrl}/patterns/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});

