import {Component, Output, EventEmitter, Input} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CollectionEntity} from '../../model/collection.entity';
import {AppComponent} from '../../app.component';

@Component({
  selector: 'app-collections',
  templateUrl: './collections.component.html',
  styleUrls: ['./collections.component.scss']
})
export class CollectionsComponent {
  category = '';
  public = true;
  @Output()
  collectionCreated = new EventEmitter<CollectionEntity>();
  @Input()
  userId = '';

  constructor(
    private readonly httpClient: HttpClient
  ) {
  }

  createCollection(): void {
    this.httpClient.post<CollectionEntity>('http://localhost:8080/api/collection', {
      category: this.category,
      public: this.public,
      creatorId: this.userId
    }).subscribe();
  }
}
