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
  activeFlashcardIndex = 0;
  flashcards: Array<FlashcardEntity> = [];
  private pCollection!: CollectionEntity;
  showDefinition = false;
  showCreate = false;
  showEdit = false;
  createTerm = '';
  createDefinition = '';
  editTerm = '';
  editDefinition = '';

  @Input()
  user: UserEntity | null = null;

  @Input()
  set collection(value: CollectionEntity) {
    this.loadFlashcards(value);
    this.pCollection = value;
  }

  get collection(): CollectionEntity {
    return this.pCollection;
  }

  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastr: ToastrService
  ) {
  }

  private loadFlashcards(collection: CollectionEntity): void {
    if (!collection) {
      this.toastr.error('Fetching flashcards went wrong: no collection given');
    } else {
      this.httpClient.get<FlashcardEntity[]>(`/api/collection/${collection.id}`, {withCredentials: true})
        .subscribe(response => {
          this.flashcards = response;
        });
    }
  }

  createFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.post<FlashcardEntity>(`/api/collection/${this.collection.id}`, {
        term: this.createTerm,
        definition: this.createDefinition,
      }, {withCredentials: true}).subscribe(response => {
        this.flashcards.push(response);
        this.toastr.success(`Flashcard ${response.term} successfully added`);
        this.createTerm = '';
        this.createDefinition = '';
      }, error => {
        this.toastr.error(error.statusText, error.status);
      });
    } else {
      this.toastr.error('Something went wrong');
    }
  }

  editFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.put<FlashcardEntity>(`/api/flashcard/${this.collection.id}/${this.flashcards[this.activeFlashcardIndex].id}`, {
        id: this.flashcards[this.activeFlashcardIndex].id,
        term: this.editTerm,
        definition: this.editDefinition
      }, {withCredentials: true}).subscribe(response => {
        this.toastr.success(`Flashcard ${response.term} successfully edited`);
        this.flashcards[this.activeFlashcardIndex] = response;
      }, error => {
        this.toastr.error(error.statusText, error.status);
      });
    } else {
      this.toastr.error('Something went wrong');
    }
  }

  deleteFlashcard(): void {
    if (!this.user) {
      this.toastr.error('Not authorised!');
    } else if (this.user.id === this.collection.creator.id) {
      this.httpClient.delete(`/api/flashcard/${this.collection.id}/${this.flashcards[this.activeFlashcardIndex].id}`, {
        withCredentials: true,
        responseType: 'text'
      })
        .subscribe(() => {
          this.toastr.success('Flashcard has been removed');
          this.flashcards.slice(this.activeFlashcardIndex, 1);

          if (this.flashcards.length > 0) {
            this.activeFlashcardIndex = (this.activeFlashcardIndex - 1 + this.flashcards.length) % this.flashcards.length;
          }

          this.loadFlashcards(this.collection);
        }, error => {
          this.toastr.error(error.responseText, error.response);
        });
    }
  }
}
