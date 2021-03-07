import {Component, Input} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';
import {UserEntity} from '../../model/user.entity';
import {CollectionEntity} from '../../model/collection.entity';
import {FlashcardEntity} from '../../model/flashcard.entity';

@Component({
  selector: 'app-flashcards',
  templateUrl: './flashcards.component.html',
  styleUrls: ['./flashcards.component.scss']
})
export class FlashcardsComponent {
  flashcards: Set<FlashcardEntity> | null = null;
  @Input()
  user: UserEntity | null = null;
  @Input()
  collection: CollectionEntity | null = null;
  firstLoad = true;

  constructor(
    private readonly httpClient: HttpClient,
    private toastr: ToastrService
  ) {
    this.loadFlashcards();
  }

  private loadFlashcards(): void {
    if (!this.collection && this.firstLoad) {
    } else if (!this.collection) {
      this.toastr.error('Fetching flashcards went wrong: no collection given');
    } else {
      this.httpClient.get<FlashcardEntity[]>(`http://192.168.0.165:8080/api/collection/${this.collection.id}`, {withCredentials: true})
        .subscribe(u => {
          console.log(u);
          this.flashcards = new Set(u);
        });
    }
    this.firstLoad = false;
  }
}
