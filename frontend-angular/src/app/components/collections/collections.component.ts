import {Component, Input, Output, EventEmitter} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CollectionEntity} from '../../model/collection.entity';
import {UserEntity} from '../../model/user.entity';
import {ToastrService} from 'ngx-toastr';

@Component({
    selector: 'app-collections',
    templateUrl: './collections.component.html',
    styleUrls: ['./collections.component.scss']
})
export class CollectionsComponent {
    category = '';
    public = false;
    @Input()
    user: UserEntity | null = null;
    @Output()
    createdCollection = new EventEmitter<CollectionEntity>();

    constructor(
        private readonly httpClient: HttpClient,
        private readonly toastr: ToastrService
    ) {
    }

    createCollection(): void {
        if (!this.user) {
            this.toastr.error('Not authorised!');
        } else {
            this.httpClient.post<CollectionEntity>('/api/collection', {
                category: this.category,
                public: this.public,
                creatorId: this.user.id
            }, {withCredentials: true}).subscribe(response => {
                this.toastr.success(`Collection ${response.category} was created`);
                this.createdCollection.emit(response);
            }, error => {
                this.toastr.error('Collection creation went wrong', error.status);
            });
        }
    }
}
